package com.pjzhong.chat.server;

import com.pjzhong.chat.protobuf.MessageProtobuf.Msg;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;

/**
 * @author zhongjp
 * @since 2018/7/6
 */
public class ChatServerHandler extends SimpleChannelInboundHandler<Msg> {

  private ChatServer server;

  public ChatServerHandler(ChatServer server) {
    this.server = server;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Msg s) throws Exception {
    Channel incoming = ctx.channel();
    server.boardCast(Msg.newBuilder()
        .setBody(String.format("[%s]: %s", incoming.attr(ServerConst.NAME).get(), s.getBody()))
        .build());
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    System.out.format("NettyTcpClient:%s online\n", ctx.channel().remoteAddress());
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    Attribute<String> name = ctx.channel().attr(ServerConst.NAME);
    System.out
        .format("NettyTcpClient:%s-%s offline\n", ctx.channel().remoteAddress(), name.get());
  }


  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) {
    Channel incoming = ctx.channel();
    if (incoming.hasAttr(ServerConst.NAME)) {
      Attribute<String> name = incoming.attr(ServerConst.NAME);
      String sb = "------------------------------------------------------\n"
          + String.format("[SERVER] - %s  is leaved, %d person(s) in this room\n",
          name.get(), server.onLine())
          + "------------------------------------------------------\n";
      server.boardCast(Msg.newBuilder().setBody(sb).build());
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    System.out.format("NettyTcpClient:%s error\n", ctx.channel().remoteAddress());
    cause.printStackTrace();
    ctx.close();
  }

}
