package com.Lab.Msg;

public class StateMsg {
    private final MsgStatus status;

    public StateMsg(MsgStatus status ) {
        this.status = status;
    }

    public MsgStatus getStatus(){
        return status;
    }
}
