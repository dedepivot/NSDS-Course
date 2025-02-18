package com.Lab.Msg;

public class PutMsg {
    final private String name;
    final private String address;

    public PutMsg(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}
