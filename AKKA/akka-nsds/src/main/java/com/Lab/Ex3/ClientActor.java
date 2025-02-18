package com.Lab.Ex3;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.Lab.Msg.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientActor extends AbstractActor{
    //What to do when we receive msgs
    @Override
    public AbstractActor.Receive createReceive() {
        return receiveBuilder()
                .match(StartClientMsg.class, this::onStartClientMsg)
                .match(ServerAnswerMsg.class, this::onServerAnswerMsg)
                .build();
    }

    private void onStartClientMsg(StartClientMsg startMsg){
        ActorRef server = startMsg.getServer();
        server.tell(new PutMsg("Sam", "sam@gmail.com"), self());
        server.tell(new PutMsg( "Ferrari", "ferrari@worldChampions.com"), self());
        server.tell(new GetMsg("Ferrari"), self());
        server.tell(new GetMsg("Sam"), self());
    }
    private void onServerAnswerMsg(ServerAnswerMsg msg){
        System.out.println(msg.getAddress());
    }

    static Props props() {
        return Props.create(ClientActor.class);
    }
}
