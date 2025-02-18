package com.Eval22;

import akka.actor.ActorRef;

public class InitializeMsgBroker {
    private final ActorRef broker;

    public InitializeMsgBroker(ActorRef broker) {
        this.broker = broker;
    }

    public ActorRef getBroker() {
        return broker;
    }
}
