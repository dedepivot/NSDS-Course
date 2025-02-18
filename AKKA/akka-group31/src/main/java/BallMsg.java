import akka.actor.ActorContext;
import akka.actor.ActorRef;

public class BallMsg {
	public static final int COUNTERCLOCKWISE = 0;
	public static final int CLOCKWISE = 1;

	private final int logic;
    public BallMsg(int logic) {
        this.logic = logic;
    }
	public int getLogic() {
		return logic;
	}
}
