package com.Lab.Ex5;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.Lab.Msg.GetMsg;
import com.Lab.Msg.PutMsg;
import com.Lab.Msg.ServerAnswerMsg;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

	private void receivePutMsg(PutMsg msg) throws Exception {
		if(!msg.getName().equals("Fails")){
			contactBook.put(msg.getName(), msg.getAddress());
		}else{
			System.out.println("I'm failing...");
			throw new Exception();
		}

	}

	@Override
	public void preRestart(Throwable reason, Optional<Object> message) {
		System.out.print("Preparing to restart...");
	}

	@Override
	public void postRestart(Throwable reason) {
		System.out.println("...now restarted!");
	}

	static Props props() {
		return Props.create(ServerActor.class);
	}

}
