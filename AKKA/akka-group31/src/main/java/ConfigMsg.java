import akka.actor.ActorRef;

public class ConfigMsg {
    private final ActorRef clockwise;
    private final ActorRef counterClockwise;

    public ConfigMsg(ActorRef clockwise, ActorRef counterClockwise) {
        this.clockwise = clockwise;
        this.counterClockwise = counterClockwise;
    }

    public ActorRef getClockwise() {
        return clockwise;
    }

    public ActorRef getCounterClockwise() {
        return counterClockwise;
    }
}
