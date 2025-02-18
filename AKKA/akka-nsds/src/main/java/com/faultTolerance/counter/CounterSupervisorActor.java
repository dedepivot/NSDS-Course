package com.faultTolerance.counter;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import java.time.Duration;

public class CounterSupervisorActor extends AbstractActor {

	// #strategy
	// Define strategy in the case of failure detected. In this case for every failure "Exception" (so everyone)
	// Restart the actor
    private static SupervisorStrategy strategy =
        new OneForOneStrategy(
            1, // Max no of retries
            Duration.ofMinutes(1), // Within what time period
			//SupervisorStrategy.restart() restart from SCRATCH (no previous state) the faulty actor
			//SupervisorStrategy.resume() start from the previous SAVED state (before the faultiness)
			//SupervisorStrategy.stop() stop and DO NOT restart the faulty actor
            DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.stop())
                .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
      return strategy;
    }

	public CounterSupervisorActor() {
	}

	//CounterSupervisorActor can only receive a msg asking to CREATE new actor and manage them
	//The msg will contain the props made from the class that need to be "acted" {Props.create(CounterActor.class)}
	@Override
	public Receive createReceive() {
		// Creates the child actor within the supervisor actor context
		return receiveBuilder()
		          .match(Props.class,
		              props -> {
					  //For every received msg (props) answer with a ActorRef of the newly created actor {getContext().actorOf(props)}
		                getSender().tell(getContext().actorOf(props), getSelf());
		              })
		          .build();
	}

	static Props props() {
		return Props.create(CounterSupervisorActor.class);
	}

}
