package com.Eval22;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Subscriber extends AbstractActor {
    private ActorRef broker;
    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(InitializeMsgBroker.class, m->{this.broker = m.getBroker();})
                .match(SubscribeMsg.class, m->this.broker.tell(m, self()))
                .match(NotifyMsg.class, m->{System.out.println(m.getValue());})
                .build();
    }

    static Props props() {
        return Props.create(Subscriber.class);
    }
}
