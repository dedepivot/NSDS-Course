package com.Lab.Ex5;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.Lab.Msg.GetMsg;
import com.Lab.Msg.PutMsg;
import com.Lab.Msg.ServerAnswerMsg;
import com.Lab.Msg.StartClientMsg;

public class ClientActor extends AbstractActor{
    //What to do when we receive msgs
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartClientMsg.class, this::onStartClientMsg)
                .match(ServerAnswerMsg.class, this::onServerAnswerMsg)
                .build();
    }

    private void onStartClientMsg(StartClientMsg startMsg) throws InterruptedException {
        ActorRef server = startMsg.getServer();
        server.tell(new PutMsg("Sam", "sam@gmail.com"), self());
        server.tell(new PutMsg( "Ferrari", "ferrari@worldChampions.com"), self());
        server.tell(new GetMsg("Ferrari"), self());
        server.tell(new GetMsg("Sam"), self());
        server.tell(new PutMsg("Fails", "a@b.com"), self());
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
