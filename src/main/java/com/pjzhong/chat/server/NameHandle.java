package com.pjzhong.chat.server;

import com.pjzhong.chat.protobuf.MessageProtobuf.Msg;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.StringUtil;

public class NameHandle extends SimpleChannelInboundHandler<Msg> {

  private ChatServer chatServer;

  public NameHandle(ChatServer chatServer) {
    this.chatServer = chatServer;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    Channel incoming = ctx.channel();
    incoming.writeAndFlush(Msg.newBuilder().setBody("What is Your name:").build());
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Msg msg) throws Exception {
    Channel channel = ctx.channel();
    if (channel.hasAttr(ServerConst.NAME)) {
      ctx.fireChannelRead(msg);
    } else {
      if (StringUtil.isNullOrEmpty(msg.getBody())) {
        channel.writeAndFlush(Msg.newBuilder().setBody("What is Your name:").build());
      } else {
        chatServer.addChannel(channel);
        channel.attr(ServerConst.NAME).set(msg.getBody());
        String join = "------------------------------------------------------\n"
            + String.format("[SERVER] - Welcome %s, %d person(s) in this room\n",
            msg.getBody(), chatServer.onLine())
            + "------------------------------------------------------";
        chatServer.boardCast(Msg.newBuilder().setBody(join).build());
        System.out
            .format("NettyTcpClient:%s-%s is connected\n", channel.remoteAddress(), msg.getBody());
      }
    }
  }


}
