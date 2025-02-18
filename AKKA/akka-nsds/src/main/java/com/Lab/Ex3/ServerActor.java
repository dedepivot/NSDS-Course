package com.Lab.Ex3;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.Lab.Msg.*;

import java.util.HashMap;
import java.util.Map;

public class ServerActor extends AbstractActor{

	Map<String, String> contactBook;

	public ServerActor() {
		contactBook = new HashMap<>();
	}

	//What to do when we receive msgs
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(GetMsg.class, this::receiveGetMsg)
				.match(PutMsg.class, this::receivePutMsg)
				.build();
	}

	private void receiveGetMsg(GetMsg msg){
		sender().tell(new ServerAnswerMsg(contactBook.get(msg.getName())), self());
	}

	private void receivePutMsg(PutMsg msg){
		contactBook.put(msg.getName(), msg.getAddress());
	}

	static Props props() {
		return Props.create(ServerActor.class);
	}

}
