import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class KeepAway {

	public final static int W = 2;
	public final static int R = 3;

	public static void main(String[] args) {
		
		final ActorSystem sys = ActorSystem.create("System");

		ActorRef a = sys.actorOf(BallPasserActor.props(), "a");
		ActorRef b = sys.actorOf(BallPasserActor.props(), "b");
		ActorRef c = sys.actorOf(BallPasserActor.props(), "c");
		ActorRef d = sys.actorOf(BallPasserActor.props(), "d");

		a.tell(new ConfigMsg(b,d), ActorRef.noSender());
		b.tell(new ConfigMsg(c, a), ActorRef.noSender());
		c.tell(new ConfigMsg(d, b), ActorRef.noSender());
		d.tell(new ConfigMsg(a, c), ActorRef.noSender());

		// Wait until system is ready
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// A sends a ball clockwise, it receives it back and drops it
		a.tell(new SendBallMsg(BallMsg.CLOCKWISE), ActorRef.noSender());

		// Wait until system is ready
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// B sends a ball counterclockwise, it receives it back and drops it
		b.tell(new SendBallMsg(BallMsg.COUNTERCLOCKWISE), ActorRef.noSender());

		// Wait until system is ready
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// C sends a ball counterclockwise, the ball gets to D that is put to rest
		c.tell(new SendBallMsg(BallMsg.COUNTERCLOCKWISE), ActorRef.noSender());

		// Wait until system is ready
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// D sends a ball clockwise, but it's resting
		d.tell(new SendBallMsg(BallMsg.CLOCKWISE), ActorRef.noSender());

		// D sends another ball clockwise, it's now to R balls while resting and resumes
		d.tell(new SendBallMsg(BallMsg.COUNTERCLOCKWISE), ActorRef.noSender());

		// C gets back its own ball and drops it
		// D eventually gets two balls back and drops them, no other player is put to rest

		// Wait until system is ready again
		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
