package com.pjzhong.chat.client;

import com.pjzhong.chat.protobuf.MessageProtobuf.Msg;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.Scanner;
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
  private ExecutorService worker;

  private final String host;
  private final int port;

  public NettyTcpClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  @Override
  public void init() {
    worker = Executors.newSingleThreadExecutor();
    resetConnect();
  }

  @Override
  public void resetConnect() {
    if (!isClosed) {
      worker.execute(this::closeChannel);
      worker.execute(new ReconnectRunnable(this));
    }
  }

  @Override
  public void close() {
    if (isClosed) {
      return;
    }
    if (worker != null) {
      worker.execute(this::closeChannel);
      worker.execute(this::cleanBootstrap);
      closeExecutor(worker);
    }
    worker = null;
    isClosed = true;
    System.out.println("TCP Client closed");
  }

  private void closeExecutor(ExecutorService executor) {
    if (executor != null) {
      try {
        executor.shutdown();
        executor.awaitTermination(ClientCons.WAIT_TO_CLOSE, TimeUnit.MINUTES);
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
        channel = null;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void cleanBootstrap() {
    if (bootstrap != null) {
      bootstrap.config().group().shutdownGracefully();
      bootstrap = null;
    }
  }

  private class ReconnectRunnable implements Runnable {

    private TcpClient client;

    public ReconnectRunnable(TcpClient client) {
      this.client = client;
    }

    @Override
    public void run() {
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
          .handler(new ChatClientInitializer(client));
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

  @Override
  public void execute(Runnable runnable) {
    worker.execute(runnable);
  }

  public static void main(String[] args) {
    NettyTcpClient client = new NettyTcpClient("localhost", 8888);
    client.init();

    Runtime.getRuntime().addShutdownHook(new Thread(client::close));
    Thread t = new Thread(() -> {
      try (Scanner s = new Scanner(System.in)) {
        Msg msg = Msg.newBuilder()
            .setBody(s.nextLine())
            .build();
        client.sendMsg(msg);
      }
    });
    t.setDaemon(true);
    t.run();
  }
}
