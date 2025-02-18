package com.faultTolerance.counter;

import java.util.Optional;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class CounterActor extends AbstractActor {

	private int counter;

	public CounterActor() {
		this.counter = 0;
	}

	//What to do on receive? Call function "onMessage"
	@Override
	public Receive createReceive() {
		return receiveBuilder().match(DataMessage.class, this::onMessage).build();
	}

	//Change behaviour based on msg content
	void onMessage(DataMessage msg) throws Exception {
		if (msg.getCode() == CounterSupervisor.NORMAL_OP) {
			System.out.println("I am executing a NORMAL operation...counter is now " + (++counter));
		} else if (msg.getCode() == CounterSupervisor.FAULT_OP) {
			System.out.println("I am emulating a FAULT!");
			//thrown an Exception that will be catch by the supervisor
			throw new Exception("Actor fault!"); 
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
		return Props.create(CounterActor.class);
	}

}
