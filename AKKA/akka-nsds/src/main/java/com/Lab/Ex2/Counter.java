package com.Lab.Ex2;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.Lab.Msg.MsgStatus;
import com.Lab.Msg.StateMsg;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Counter {

	private static final int numThreads = 10;
	private static final int numMessages = 100;

	public static void main(String[] args) {

		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef counter = sys.actorOf(CounterActor.props(), "counter");

		// Send messages from multiple threads in parallel
		final ExecutorService exec = Executors.newFixedThreadPool(numThreads);

		counter.tell(new StateMsg(MsgStatus.DECREMENT), ActorRef.noSender()); //stash 1) compute
		counter.tell(new StateMsg(MsgStatus.DECREMENT), ActorRef.noSender()); //stash 1) unstash (unstashAll and restash) 2) compute
		counter.tell(new StateMsg(MsgStatus.INCREMENT), ActorRef.noSender()); //add 1)
		counter.tell(new StateMsg(MsgStatus.DECREMENT), ActorRef.noSender()); //stash 2) unstash (unstashAll and restash) 3) compute
		counter.tell(new StateMsg(MsgStatus.INCREMENT), ActorRef.noSender()); //add 2)
		counter.tell(new StateMsg(MsgStatus.INCREMENT), ActorRef.noSender()); //add 3)

		// Wait for all messages to be sent and received
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		exec.shutdown();
		sys.terminate();

	}

}
