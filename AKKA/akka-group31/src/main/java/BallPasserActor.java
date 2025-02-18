import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.HashMap;
import java.util.Map;

public class BallPasserActor extends AbstractActorWithStash {
    final private Map<Integer, ActorRef> neighbours;
    private int counter;
    public BallPasserActor() {
        neighbours = new HashMap<>();
    }
    @Override
    public Receive createReceive() {
        return playing();
    }

    public Receive playing(){
        return receiveBuilder()
                .match(ConfigMsg.class, this::onConfig)
                .match(SendBallMsg.class, this::onSend)
                .match(RestingSendBallMsg.class, m->{
                    neighbours.get(m.getLogic()).tell(new RestingBallMsg(m.getLogic()), self());
                    System.out.println("Ball sent to: " + neighbours.get(m.getLogic()) + " original sent by me: " + self());
                })
                .match(RestingBallMsg.class, this::onRestingBall)
                .match(BallMsg.class, this::onBall)
                .build();
    }
    private void onSend(SendBallMsg msg){
        neighbours.get(msg.getLogic()).tell(new BallMsg(msg.getLogic()), self());
        System.out.println("Ball sent to: " + neighbours.get(msg.getLogic()) + " original sent by me: " + self());
    }

    private void onBall(BallMsg msg){
        if(sender().equals(self())){
            System.out.println("Drop " + self());
        }else{
            counter++;
            if(counter>KeepAway.W){
                counter=0;
                System.out.println("I'm resting " + self());
                getContext().become(resting());
                self().tell(new RestingBallMsg(msg.getLogic()), sender());
            }else{
                neighbours.get(msg.getLogic()).tell(msg, sender());
                System.out.println("Ball sent to: " + neighbours.get(msg.getLogic()) + " original sent by: " + sender());
            }
        }
    }

    private void onRestingBall(RestingBallMsg msg){
        if(sender().equals(self())){
            System.out.println("Drop " +self());
        }else{
            neighbours.get(msg.getLogic()).tell(msg, sender());
            System.out.println("Ball sent to: " + neighbours.get(msg.getLogic()) + " original sent by: " + sender());
        }
    }

    private void onConfig(ConfigMsg msg){
        neighbours.put(BallMsg.COUNTERCLOCKWISE, msg.getCounterClockwise());
        neighbours.put(BallMsg.CLOCKWISE, msg.getClockwise());
        //Each time a new Config msg is sent a new match is started with the same (rested) actor
        counter = 0;
    }

    public Receive resting(){
        return receiveBuilder()
                .match(SendBallMsg.class,  m->{self().tell(new RestingSendBallMsg(m.getLogic()), self());})
                .match(RestingSendBallMsg.class, this::receiveOnResting)
                .match(RestingBallMsg.class, this::receiveOnResting)
                .match(BallMsg.class, m->{self().tell(new RestingBallMsg(m.getLogic()), sender());})
                .build();
    }

    private void receiveOnResting(RestingBallMsg msg){
        counter++;
        stash();
        if(counter==KeepAway.R){
            counter=0;
            System.out.println("I'm playing again");
            getContext().become(playing());
            unstashAll();
        }
    }

    static Props props() {
        return Props.create(BallPasserActor.class);
    }
}
