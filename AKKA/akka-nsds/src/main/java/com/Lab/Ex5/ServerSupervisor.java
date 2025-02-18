package com.Lab.Ex5;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;

import java.time.Duration;

public class ServerSupervisor extends AbstractActor {
    private static SupervisorStrategy strategy =
            new OneForOneStrategy(
                    1, // Max no of retries
                    Duration.ofMinutes(1), // Within what time period
                    DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume())
                            .build());
    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    public Receive createReceive() {
        // Creates the child actor within the supervisor actor context
        return receiveBuilder()
                .match(Props.class,
                        props -> {
                            //return ActorRef server
                            getSender().tell(getContext().actorOf(props), getSelf());
                        })
                .build();
    }
    static Props props() {
        return Props.create(ServerSupervisor.class);
    }
}
