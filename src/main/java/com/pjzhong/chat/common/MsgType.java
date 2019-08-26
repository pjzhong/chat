package com.pjzhong.chat.common;

public enum MsgType {

  AUTH(110),
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
