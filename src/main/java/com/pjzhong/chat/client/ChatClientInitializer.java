package com.pjzhong.chat.client;

import com.pjzhong.chat.protobuf.MessageProtobuf;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;

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
    pipeline.addLast(HeartbeatHandler.class.getSimpleName(), new HeartbeatHandler(tcpClient));
    pipeline.addLast("handler", new ChatClientHandler());
  }
}
