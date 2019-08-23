@echo off

set PROTO_PATH=src\main\resources\protobuf
set JAVA_OUT=src\main\java

for /f %%i in ('dir /b "%PROTO_PATH%\*.proto"') do (
     protoc -I=%PROTO_PATH% --java_out=%JAVA_OUT% %PROTO_PATH%\%%i
     echo compile %%i To java file successfully!
)