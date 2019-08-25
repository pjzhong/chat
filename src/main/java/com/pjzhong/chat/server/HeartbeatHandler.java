package com.pjzhong.chat.server;

import static com.pjzhong.chat.common.MsgType.HEART_BEAT;

import com.pjzhong.chat.protobuf.MessageProtobuf.Msg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class HeartbeatHandler extends SimpleChannelInboundHandler<Msg> {

  private Msg heartBeat;

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    super.userEventTriggered(ctx, evt);
    if (evt instanceof IdleStateEvent) {
      IdleState state = ((IdleStateEvent) evt).state();
      if (state == IdleState.ALL_IDLE) {
        ctx.close();
      }
    }
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Msg msg) {
    if (msg.getType() == HEART_BEAT.getSend()) {
      ctx.writeAndFlush(initMsg());
    } else {
      ctx.fireChannelRead(msg);
    }
  }

  private Msg initMsg() {
    if (heartBeat == null) {
      heartBeat = Msg.newBuilder()
          .setType(HEART_BEAT.getPush()).build();
    }
    return heartBeat;
  }
}
