### simple chat power by netty and protobuf

#### 如何启动(Windows)
1. 安装protobuf编译器([点此下载](https://github.com/protocolbuffers/protobuf))，并添加进*PATH*系统变量
2. 运行[proto_build.bat](proto_build.bat)编译协议
3. 启动[SimpleChatServer](src/main/java/com/pjzhong/chat/server/ChatServer.java)
4. 启动[NettyTcpClient](src/main/java/com/pjzhong/chat/client/NettyTcpClient.java)