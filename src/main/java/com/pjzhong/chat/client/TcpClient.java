package com.pjzhong.chat.client;

import com.pjzhong.chat.protobuf.MessageProtobuf;

public interface TcpClient {

  void init();

  void resetConnect();

  void close();

  boolean isClosed();

  void sendMsg(MessageProtobuf.Msg msg);

  void execute(Runnable runnable);
}
