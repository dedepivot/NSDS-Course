package com.Lab.Msg;

public class ServerAnswerMsg {
    final private String address;

    public ServerAnswerMsg(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
