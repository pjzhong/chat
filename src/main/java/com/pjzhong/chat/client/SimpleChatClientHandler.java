package com.pjzhong.chat.client;

import com.pjzhong.chat.protobuf.MessageProtobuf.Msg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author zhongjp
 * @since 2018/7/6
 */
public class SimpleChatClientHandler extends SimpleChannelInboundHandler<Msg> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Msg msg) throws Exception {
    System.out.println(msg.getBody());
  }
}
