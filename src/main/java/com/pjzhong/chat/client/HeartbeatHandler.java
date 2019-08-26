package com.pjzhong.chat.client;

import static com.pjzhong.chat.common.MsgType.AUTH;
import static com.pjzhong.chat.common.MsgType.HEART_BEAT;

import com.pjzhong.chat.protobuf.MessageProtobuf.Msg;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;

public class HeartbeatHandler extends SimpleChannelInboundHandler<Msg> {

  private TcpClient client;
  private Msg heartBeat;

  public HeartbeatHandler(TcpClient client) {
    this.client = client;
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    System.out.println("ChannelInactive");
    Channel channel = ctx.channel();
    if (channel != null) {
      channel.close();
      ctx.close();
    }
    client.reConnect();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
      throws Exception {
    super.exceptionCaught(ctx, cause);
    System.out.println("exceptionCaught");
    Channel channel = ctx.channel();
    if (channel != null) {
      channel.close();
      ctx.close();
    }
    client.reConnect();
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    super.userEventTriggered(ctx, evt);
    if (evt instanceof IdleStateEvent) {
      IdleState state = ((IdleStateEvent) evt).state();
      switch (state) {
        case READER_IDLE: {
          client.reConnect();
          break;
        }
        case WRITER_IDLE: {
          client.sendMsg(initMsg());
          break;
        }
      }
    }
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Msg msg) {
    if (msg.getType() == HEART_BEAT.getPush()) {
      //TODO Do something or nothing when HEART_BEAT response
    } else if (msg.getType() == AUTH.getPush()) {
      // Start to heart beat
      IdleStateHandler idle = new IdleStateHandler(
          ClientCons.HEART_BEAT_INTERVAL * 3, ClientCons.HEART_BEAT_INTERVAL, 0,
          TimeUnit.MILLISECONDS);

      String name = IdleStateHandler.class.getSimpleName();
      ChannelPipeline pipeline = ctx.channel().pipeline();
      pipeline.remove(name);
      pipeline.addBefore(getClass().getSimpleName(), name, idle);
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
