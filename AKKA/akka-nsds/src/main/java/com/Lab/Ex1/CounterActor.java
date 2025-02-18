package com.Lab.Ex1;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.Lab.Msg.AddMsg;
import com.Lab.Msg.MsgStatus;
import com.Lab.Msg.ReduceMsg;
import com.Lab.Msg.StateMsg;

public class CounterActor extends AbstractActor {

	private int counter;

	public CounterActor() {
		this.counter = 0;
	}

	//What to do when we receive msgs
	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(AddMsg.class, this::increaseCounter)
				.match(ReduceMsg.class, this::decreaseCounter)
				.match(StateMsg.class, this::statusCounter)
				.build();
	}

	void statusCounter(StateMsg msg) {
		if(msg.getStatus() == MsgStatus.INCREMENT){
			counter++;
			System.out.println("Counter increased to " + counter);
		} else{
			counter--;
			System.out.println("Counter decrease to " + counter);
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
