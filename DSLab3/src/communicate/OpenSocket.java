package communicate;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

import configData.Rule;
import clockService.ClockService;
import message.Message;

public class OpenSocket extends Thread {
	private ServerSocket listener;
	private LinkedList<Message> delayQueue;
	private ClockService clock;
	private ArrayList<Rule> receiveRule;

	public OpenSocket(int port, ClockService clock, ArrayList<Rule> receiveRule) throws IOException {
		listener = new ServerSocket(port);
		delayQueue = new LinkedList<Message>();
		this.clock = clock;
		this.receiveRule = receiveRule;
	}

	public void run() {
		while (true) {
			try {
				Socket socket = listener.accept();
				new WaitReceive(socket, delayQueue, clock, receiveRule).start();
			} catch (IOException e) {
				System.out.println("Exception occurs in OpenScoket");
			}
		}
	}

	public void closeSocket() throws IOException {
		listener.close();
	}
}
