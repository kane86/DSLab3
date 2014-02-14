package communicate;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

import configData.Rule;
import clockService.ClockService;
import message.Message;
import message.MessagePasser;
import message.TimeStampedMessage;

public class WaitReceive extends Thread {
	private Socket socket;
	private LinkedList<Message> delayQueue;
	private ClockService clock;
	private ArrayList<Rule> receiveRule;

	public WaitReceive(Socket s, LinkedList<Message> delayQueue, ClockService clock, ArrayList<Rule> receiveRule) {
		socket = s;
		this.delayQueue = delayQueue;
		this.clock = clock;
		this.receiveRule = receiveRule;
	}

	public void run() {
		ObjectInputStream input;
		try {
			input = new ObjectInputStream(socket.getInputStream());
			while (true) {
				TimeStampedMessage newMess = (TimeStampedMessage) input.readObject();
				clock.Update(newMess);
				switch (MessagePasser.CheckRule(newMess,
						receiveRule)) {
				case MessagePasser.drop:
					continue;
				case MessagePasser.duplicate:
					receive(newMess);
					receive(newMess);
					break;
				case MessagePasser.delay:
					delayQueue.add(newMess);
					break;
				default:
					receive(newMess);
					break;
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			//connection ends
			try {
				socket.close();
			} catch (IOException e1) {
				System.out.println("Exception in WaitReceive when closing socket");
			}
			return;
		}
	}

	private void receive(Message newMess) {
		MessagePasser.AddToRec(newMess);
		while (delayQueue.size() > 0) {
			MessagePasser.AddToRec(delayQueue.remove(0));
		}
	}
}
