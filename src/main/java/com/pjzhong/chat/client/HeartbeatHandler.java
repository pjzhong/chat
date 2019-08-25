package com.pjzhong.chat.client;

import static com.pjzhong.chat.common.MsgType.HEART_BEAT;

import com.pjzhong.chat.protobuf.MessageProtobuf.Msg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class HeartbeatHandler extends SimpleChannelInboundHandler<Msg> {

  private TcpClient client;
  private Msg heartBeat;

  public HeartbeatHandler(TcpClient client) {
    this.client = client;
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    client.close();
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    super.userEventTriggered(ctx, evt);
    if (evt instanceof IdleStateEvent) {
      IdleState state = ((IdleStateEvent) evt).state();
      switch (state) {
        case READER_IDLE: {
          client.resetConnect();
          break;
        }
        case WRITER_IDLE: {
          // client.sendMsg(initMsg());
          break;
        }
      }
    }
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Msg msg) {
    if (msg.getType() == HEART_BEAT.getPush()) {
      //TODO Do something or nothing when HEART_BEAT response
    } else {
      ctx.fireChannelRead(msg);
    }
  }

  private Msg initMsg() {
    if (heartBeat == null) {
      heartBeat = Msg.newBuilder()
          .setType(HEART_BEAT.getSend()).build();
    }

    return heartBeat;
  }
}
