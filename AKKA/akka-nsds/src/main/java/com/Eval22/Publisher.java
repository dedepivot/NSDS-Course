package com.Eval22;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Publisher extends AbstractActor{
    private ActorRef broker;

    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(InitializeMsgBroker.class, m->{this.broker = m.getBroker();})
                .match(PublishMsg.class, m->{this.broker.tell(m, self());})
                .build();
    }

    static Props props() {
        return Props.create(Publisher.class);
    }
}
