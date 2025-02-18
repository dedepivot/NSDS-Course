package com.Lab.Ex5;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.Lab.Msg.StartClientMsg;
import com.faultTolerance.counter.CounterActor;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Main {

	public static void main(String[] args) {
		//Initialize engine, client and supervisor
		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef serverSupervisor = sys.actorOf(ServerSupervisor.props(), "serverSupervisor");
		final ActorRef client = sys.actorOf(ClientActor.props(), "client");

		//Run supervisor and retrieve server
		scala.concurrent.Future<Object> waitingForServer = ask(serverSupervisor, ServerActor.props(), 5000);
		scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);
        ActorRef server;
        try {
            server = (ActorRef) waitingForServer.result(timeout, null);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
		//start client
		client.tell(new StartClientMsg(server), ActorRef.noSender());

		// Wait for all messages to be sent and received
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sys.terminate();

	}

}
