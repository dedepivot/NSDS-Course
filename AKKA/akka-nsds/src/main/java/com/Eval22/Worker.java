package com.Eval22;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.HashMap;
import java.util.Map;

public class Worker extends AbstractActor {
    Map<String, ActorRef> subscriber = new HashMap<>();
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SubscribeMsg.class, this::onSubscribe)
                .match(PublishMsg.class, this::onPublish)
                .build();
    }

    private void onSubscribe(SubscribeMsg msg){
        subscriber.put(msg.getTopic(), msg.getSender());
    }

    private void onPublish(PublishMsg msg) throws Exception{
        if(subscriber.containsKey(msg.getTopic())){
           subscriber.get(msg.getTopic()).tell(new NotifyMsg(msg.getValue()), self());
        }else{
            throw new Exception("topic not found");
        }
    }
    static Props props() {
        return Props.create(Worker.class);
    }
}
