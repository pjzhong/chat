package com.pjzhong.chat.common;

public enum MsgType {

  HEART_BEAT(120);

  MsgType(int type) {
    this.type = type;
  }

  private int type;

  public int getSend() {
    return type;
  }

  public int getPush() {
    return -type;
  }

}
