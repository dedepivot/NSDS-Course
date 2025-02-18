package com.counter;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class CounterActor extends AbstractActor {

	private int counter;

	public CounterActor() {
		this.counter = 0;
	}

	//What to do when we receive msgs
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(SimpleMessage.class, this::onMessage)
				.match(OtherMessage.class, m -> {System.out.println("OtherMsg received");})
				.build();
	}

	void onMessage(SimpleMessage msg) {
		++counter;
		System.out.println("Counter increased to " + counter);
		//Self sending of a msg each time u receive it
//		if(counter < 100){
//			self().tell(msg, self());
//		}
	}

	static Props props() {
		return Props.create(CounterActor.class);
	}

}
