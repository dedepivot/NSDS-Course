package com.Eval22;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class Broker extends AbstractActorWithStash {
    private final Map<Integer, ActorRef> mapActor = new HashMap<>();

    private static final SupervisorStrategy strategy =
            new OneForOneStrategy(
                    1, // Max no of retries
                    Duration.ofMinutes(1), // Within what time period
                    DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume())
                            .build());
    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public Receive createReceive() {
        return isOn();
    }

    public Receive isOn(){
        return receiveBuilder()
                //return ActorRef worker
                .match(Props.class, this::onWorkerCreation)
                .match(SubscribeMsg.class, this::onSubscribe)
                .match(PublishMsg.class, this::onPublish)
                .match(BatchMsg.class, this::goBatching)
                .build();
    }

    private Receive isBatching() {
        return receiveBuilder()
                .match(SubscribeMsg.class, this::putAside)
                .match(Props.class, this::putAside)
                .match(PublishMsg.class, this::putAside)
                .match(BatchMsg.class, this::goOn)
                .build();
    }

    // Changes of behavior
    private void goBatching(BatchMsg msg) {
        if(msg.isOn()){
            System.out.println("SERVER: Going to batch Mode... ");
            getContext().become(isBatching());
        }
    }

    private void goOn(BatchMsg msg){
        if(!msg.isOn()){
            System.out.println("SERVER: Going to working Mode... ");
            getContext().become(isOn());
            unstashAll();
        }
    }

    private void putAside(SubscribeMsg msg){
        stash();
    }

    private void putAside(Props msg){
        stash();
    }

    private void putAside(PublishMsg msg){
        stash();
    }

    private void onWorkerCreation(Props props){
        mapActor.put(0, getContext().actorOf(props));
        mapActor.put(1, getContext().actorOf(props));
        getSender().tell(new OkMsg(), self());
    }

    private void onSubscribe(SubscribeMsg msg){
        mapActor.get(msg.getKey()%2).tell(msg, self());
    }

    public void onPublish(PublishMsg msg){
        mapActor.get(0).tell(msg, self());
        mapActor.get(1).tell(msg, self());
    }

    static Props props() {
        return Props.create(Broker.class);
    }
}
