package com.Lab.Msg;

import akka.actor.ActorRef;

public class StartClientMsg {
    private final ActorRef server;

    public StartClientMsg(ActorRef server) {
        this.server = server;
    }

    public ActorRef getServer(){
        return server;
    }
}
