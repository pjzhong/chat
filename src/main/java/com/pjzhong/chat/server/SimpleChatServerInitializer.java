package com.pjzhong.chat.server;

import com.pjzhong.chat.protobuf.MessageProtobuf;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.flush.FlushConsolidationHandler;

/**
 * @author zhongjp
 * @since 2018/7/6
 */
public class SimpleChatServerInitializer extends ChannelInitializer<SocketChannel> {

  private SimpleChatServer server;

  public SimpleChatServerInitializer(SimpleChatServer server) {
    this.server = server;
  }

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
    pipeline.addFirst("autoflush", new FlushConsolidationHandler());//Add it consolidate flush
    pipeline.addLast("frameEncoder", new LengthFieldPrepender(2));
    pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(65535,
        0, 2, 0, 2));
    pipeline.addLast("decoder", new ProtobufEncoder());
    pipeline.addLast("encoder", new ProtobufDecoder(MessageProtobuf.Msg.getDefaultInstance()));
    pipeline.addLast("auth", new SimpleNameHandle(server));
    pipeline.addLast("handler", new SimpleChatServerHandler(server));
  }
}
