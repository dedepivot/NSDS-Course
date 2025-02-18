# Evaluation lab - Akka

## Group number: 31

## Group members

- Bossi Nicolò		|	10710040
- Grisoni Samuele	|	10810440
- Oliva Antonio		|	10819753

## Description of message flows

	At the start, a ConfigMsg is sent to all the players, telling them the next player accordingly to each cycle.
	When starting a cycle of ball passing, keepAway sends a SendBallMsg to the first player of the cycle, telling how the ball should be passed. The first player
	then sets as sender themself and pass the ball to the next player. The ball continues to be passed until it returns to the sender, and then it is dropped.
	When a player receives a BallMsg, they can either drop the ball (if they are the sender of such (Resting)BallMsg) or pass it to the next player, increasing a
	counter (except when a RestingBallMsg is passed). When the counter exceeds the threshold W, the player goes to rest. During the rest, the counter is
	reinitialized and both SendBallMsg and BallMsg received are converted respectively into RestingSendBallMsg and RestingBallMsg and stashed, while the counter is
	increased. When the counter reaches R, the player wakes up and sends (unstash) all RestingBallMsgs to the next player (according to the cycle of the message
	before resting) or drops the ball if they are the sender.
