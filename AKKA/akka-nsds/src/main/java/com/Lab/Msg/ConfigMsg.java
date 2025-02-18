package com.Lab.Msg;

import akka.actor.ActorRef;

public class ConfigMsg {
    private final ActorRef server;
    private final int clientId;

    public ConfigMsg(ActorRef server, int clientId) {
        this.server = server;
        this.clientId = clientId;
    }

    public ActorRef getServer(){
        return server;
    }

    public int getClientId() {
        return clientId;
    }
}
