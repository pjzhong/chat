package com.pjzhong.chat.client;

import com.pjzhong.chat.protobuf.MessageProtobuf;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;

/**
 * @author zhongjp
 * @since 2018/7/6
 */
public class ChatClientInitializer extends ChannelInitializer<SocketChannel> {

  private TcpClient tcpClient;

  public ChatClientInitializer(TcpClient tcpClient) {
    this.tcpClient = tcpClient;
  }

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
    pipeline.addLast("frameEncoder", new LengthFieldPrepender(2));
    pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
    pipeline.addLast("decoder", new ProtobufEncoder());
    pipeline.addLast("encoder", new ProtobufDecoder(MessageProtobuf.Msg.getDefaultInstance()));
    // 3次心跳没响应，代表连接已断开
    pipeline.addFirst("idleState", new IdleStateHandler(
        ClientCons.HEART_BEAT_INTERVAL * 3, ClientCons.HEART_BEAT_INTERVAL, 0,
        TimeUnit.MILLISECONDS));
    pipeline.addLast("heartbeat", new HeartbeatHandler(tcpClient));
    pipeline.addLast("handler", new ChatClientHandler());
  }
}
