package com.pjzhong.chat.server;

import com.pjzhong.chat.protobuf.MessageProtobuf.Msg;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author zhongjp
 * @since 2018/7/6
 */
public class ChatServer {

  private int port = 8888;
  private static ChannelGroup channels;
  private ServerBootstrap bootstrap;

  public ChatServer() {
  }

  public ChatServer(int port) {
    this.port = port;
  }

  public void run() throws Exception {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    bootstrap = new ServerBootstrap();
    bootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChatServerInitializer(this))
        .option(ChannelOption.SO_BACKLOG, 128)
        .childOption(ChannelOption.SO_KEEPALIVE, true);
    System.out.println("ChatServer has started");

    ChannelFuture f = bootstrap.bind(port).sync();
    f.channel().closeFuture().sync();
  }

  public void boardCast(Msg s) {
    if (channels != null) {
      channels.writeAndFlush(s);
    }
  }

  public long onLine() {
    return channels == null ? 0 : channels.size();
  }

  public void addChannel(Channel channel) {
    channels.add(channel);
  }

  public void close() {
    channels.newCloseFuture().awaitUninterruptibly();
    bootstrap.config().group().shutdownGracefully().awaitUninterruptibly();
    bootstrap.config().childGroup().shutdownGracefully().awaitUninterruptibly();
  }

  public static void main(String[] args) throws Exception {
    ChatServer chatServer = new ChatServer();
    chatServer.run();

    Runtime.getRuntime().addShutdownHook(new Thread(chatServer::close));
  }
}
