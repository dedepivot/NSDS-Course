package com.Eval22;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.Lab.Ex5.ServerActor;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;


public class PubSub {

	public final static String TOPIC0 = "topic0";
	public final static String TOPIC1 = "topic1";

	public static void main(String[] args) {

		final ActorSystem sys = ActorSystem.create("System");
      	final ActorRef broker = sys.actorOf(Broker.props());
		final ActorRef subscriber = sys.actorOf(Subscriber.props());
		final ActorRef publisher = sys.actorOf(Publisher.props());

		//Initialize actor
		scala.concurrent.Future<Object> waitingForWorker= ask(broker, Worker.props(), 5000);
		scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);
		OkMsg okMsg;
		try {
			okMsg = (OkMsg) waitingForWorker.result(timeout, null);
		} catch (TimeoutException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		if(okMsg!=null){
			System.out.println("Worker are online");
		}
        publisher.tell(new InitializeMsgBroker(broker), ActorRef.noSender());
		subscriber.tell(new InitializeMsgBroker(broker), ActorRef.noSender());


		//Some example subscriptions
		subscriber.tell(new SubscribeMsg(TOPIC0, subscriber), ActorRef.noSender());
		subscriber.tell(new SubscribeMsg(TOPIC1, subscriber), ActorRef.noSender());
		
		// Waiting for subscriptions to propagate
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Some example events
		publisher.tell(new PublishMsg(TOPIC0, "Test event 1"), ActorRef.noSender());
		publisher.tell(new PublishMsg(TOPIC1, "Test event 2"), ActorRef.noSender());
		
		// Waiting for events to propagate
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Turn the broker in batching mode
		broker.tell(new BatchMsg(true), ActorRef.noSender());

		// Waiting for messages to propagate
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// More example events
		publisher.tell(new PublishMsg(TOPIC0, "Test message 3"), ActorRef.noSender());
		publisher.tell(new PublishMsg(TOPIC1, "Test message 4"), ActorRef.noSender());
		
		// Waiting for events to propagate
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		broker.tell(new BatchMsg(false), ActorRef.noSender());
		// In this example, the last two events shall not be processed until after this point

		// Wait for all messages to be sent and received
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sys.terminate();
	}

}
