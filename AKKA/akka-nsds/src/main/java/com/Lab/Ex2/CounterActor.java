package com.Lab.Ex2;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithStash;
import akka.actor.Props;
import com.Lab.Msg.AddMsg;
import com.Lab.Msg.MsgStatus;
import com.Lab.Msg.ReduceMsg;
import com.Lab.Msg.StateMsg;

public class CounterActor extends AbstractActorWithStash {

	private int counter;

	public CounterActor() {
		this.counter = 0;
	}

	//What to do when we receive msgs
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(StateMsg.class, this::receiveMsg)
				.build();
	}

	void receiveMsg(StateMsg msg) {
		if(msg.getStatus() == MsgStatus.INCREMENT){
			counter++;
			System.out.println("Counter increased to " + counter);
			unstashAll();
		} else{
			if(counter==0){
				System.out.println("Messaged stashed");
				stash();
			}else{
				counter--;
				System.out.println("Counter decrease to " + counter);
			}
		}
	}

	void increaseCounter(AddMsg msg) {
		counter++;
		System.out.println("Counter increased to " + counter);
	}
	void decreaseCounter(ReduceMsg msg) {
		counter--;
		System.out.println("Counter decrease to " + counter);
	}


	static Props props() {
		return Props.create(CounterActor.class);
	}

}
