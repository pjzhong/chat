package com.pjzhong.chat.client;

import com.pjzhong.chat.protobuf.MessageProtobuf.Head;
import com.pjzhong.chat.protobuf.MessageProtobuf.Msg;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author zhongjp
 * @since 2018/7/6
 */
public class NettyTcpClient implements TcpClient {

  private Bootstrap bootstrap;
  private Channel channel;
  private boolean isClosed;
  private boolean isReconnecting;
  private ExecutorService worker;

  private final String host;
  private final int port;

  public NettyTcpClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  @Override
  public void init() {
    close();
    isClosed = false;
    worker = Executors.newSingleThreadExecutor();
    resetConnect();
  }

  @Override
  public void resetConnect() {
    if (!isClosed && !isReconnecting) {
      synchronized (this) {
        if (!isClosed && !isReconnecting) {
          isReconnecting = true;
          closeChannel();
          worker.execute(new ReconnectRunnable());
        }
      }
    }
  }

  private void initBootstrap() {
    EventLoopGroup loopGroup = new NioEventLoopGroup(4);
    bootstrap = new Bootstrap()
        .group(loopGroup)
        .channel(NioSocketChannel.class)
        // 设置该选项以后，如果在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文
        .option(ChannelOption.SO_KEEPALIVE, true)
        // 设置禁用nagle算法
        .option(ChannelOption.TCP_NODELAY, true)
        // 连接超时
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 200)
        .handler(new SimpleChatClientInitializer(this));
  }


  @Override
  public void close() {
    if (isClosed) {
      return;
    }

    closeChannel();
    closeExecutor(worker);
    if (bootstrap != null) {
      bootstrap.config().group().shutdownGracefully();
      bootstrap = null;
      System.out.println("TCP Client closed");
    }
    isClosed = true;
  }

  private void closeExecutor(ExecutorService executor) {
    if (executor != null) {
      try {
        executor.shutdown();
        executor.awaitTermination(3, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void closeChannel() {
    if (channel != null) {
      try {
        channel.close();
        channel.eventLoop().shutdownGracefully().await();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private class ReconnectRunnable implements Runnable {

    @Override
    public void run() {
      try {
        for (int i = 0; i < ClientCons.RECONNECT_TYPE && !isClosed; i++) {
          if (connectServer()) {
            break;
          }

          try {
            System.out.format("reconnect after %s mills%n", ClientCons.RECONNECT_INTERVAL);
            TimeUnit.MILLISECONDS.sleep(ClientCons.RECONNECT_INTERVAL);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      } finally {
        isReconnecting = false;
      }
    }

    private boolean connectServer() {
      boolean suc = true;
      try {
        if (bootstrap != null) {
          try {
            bootstrap.config().group().shutdownGracefully();
          } finally {
            bootstrap = null;
          }
        }
        initBootstrap();
        channel = bootstrap.connect(host, port).sync().channel();
        System.out.format("connect to server-%s:%s success%n", host, port);
      } catch (Exception e) {
        System.err.format("connect to server-%s:%s failed%n", host, port);
        channel = null;
        suc = false;
      }
      return suc;
    }
  }

  @Override
  public boolean isClosed() {
    return isClosed;
  }

  @Override
  public void sendMsg(Msg msg) {
    channel.writeAndFlush(msg);
  }

  public static void main(String[] args) {
    NettyTcpClient client = new NettyTcpClient("localhost", 8888);
    client.init();

    Runtime.getRuntime().addShutdownHook(new Thread(client::close));

    Scanner scanner = new Scanner(System.in);
    while (true) {
      String s = scanner.nextLine();
      Msg msg = Msg.newBuilder()
          .setHead(
              Head.newBuilder()
                  .setType(100)
                  .build())
          .setBody(s)
          .build();
      client.sendMsg(msg);
    }
  }
}
