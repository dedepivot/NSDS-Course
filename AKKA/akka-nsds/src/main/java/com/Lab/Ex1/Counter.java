package com.Lab.Ex1;

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

		for (int i = 0; i < numMessages; i++) {
			//exec.submit(() -> counter.tell(new AddMsg(), ActorRef.noSender()));
			//exec.submit(() -> counter.tell(new ReduceMsg(), ActorRef.noSender()));
            if(i%2 == 0){
				exec.submit(() -> counter.tell(new StateMsg(MsgStatus.DECREMENT), ActorRef.noSender()));
			}else{
				exec.submit(() -> counter.tell(new StateMsg(MsgStatus.INCREMENT), ActorRef.noSender()));
			}

		}
		
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
