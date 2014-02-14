package message;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import clockService.ClockService;
import clockService.LogicalClock;
import clockService.VectorClock;
import communicate.OpenSocket;
import configData.IpPort;
import configData.Rule;
import configData.YamlParser;

public class MessagePasser {
	public static final int drop = 0;
	public static final int duplicate = 1;
	public static final int delay = 2;

	public static LinkedList<Message> sendBuffer;
	protected static LinkedList<Message> receiveBuffer;
	public static YamlParser configFile;
	protected String localName;
	protected int seqNum = 1;
	protected HashSet<String> socketStatus;
	protected HashMap<String, ObjectOutputStream> socketSend;
	protected String configFileName;
	protected ClockService clock;

	public MessagePasser(String configuration_filename, String local_name)
			throws IOException {
		sendBuffer = new LinkedList<Message>();
		receiveBuffer = new LinkedList<Message>();
		socketSend = new HashMap<String, ObjectOutputStream>();
		socketStatus = new HashSet<String>();
		this.localName = local_name;
		this.configFileName = configuration_filename;

		configFile = new YamlParser(configuration_filename);
		this.localName = local_name;
	
		// setup time
		if (configFile.clockType.equals("vector")) {
			clock = new VectorClock(configFile.configList.size() - 1,
					configFile.nodeList.get(local_name).index);
		} else {
			clock = new LogicalClock();
		}

		// open own port
		OpenSocket open;
		if (localName.equals("logServer")) {
			open = new OpenSocket(configFile.nodeList.get(localName).port, clock,
					configFile.logRules);
		} else {
			open = new OpenSocket(configFile.nodeList.get(localName).port, clock,
					configFile.receiveRules);
		}
		open.start();
		System.out.println(localName + " server running...");
	}

	public ClockService GetClock() {
		return clock;
	}

	public String GetLocalName() {
		return localName;
	}

	public void connect(String name) throws UnknownHostException, IOException {
		IpPort server = configFile.nodeList.get(name);
		System.out.println("Trying to connect to " + name + " ip:" + server.ip
				+ " port:" + server.port);
		@SuppressWarnings("resource")
		Socket socket = new Socket(server.ip, server.port);
		socketSend.put(name, new ObjectOutputStream(socket.getOutputStream()));
	}

	public void send(Message message) {
		// check whether configuration file has been modified
		// re-read file if it is modified
		if (!configFile.CheckFile(configFileName)) {
			try {
				configFile.ReadFile(configFileName);
			} catch (IOException e) {
				System.out.println("Error! Can't access to file "
						+ configFileName);
			}
		}

		// match rule
		switch (CheckRule(message, configFile.sendRules)) {
		case drop:
			System.out.println("Sending dropped");
			return;
		case duplicate:
			SendMess(message, false);
			seqNum--;
			SendMess(message, true);
			break;
		case delay:
			SetMess(message, false);
			sendBuffer.add(message);
			break;
		default:
			SendMess(message, false);
		}

	}

	private void SendMess(Message message, boolean dupe) {
		SetMess(message, dupe);
		//send message to destination
		SendAction(message, message.getDest());
		//copy message to log server
		if (true == message.get_log()) {
		    SendAction(message, "logServer");
		}
		while (sendBuffer.size() > 0) {
			Message m = sendBuffer.remove(0); 
			SendAction(m, m.getDest());
			if (m.get_log() == true) {
			    SendAction(m, "logServer");
			}
		}
	}

	private void SetMess(Message message, boolean dupe) {
		// set message
		message.set_seqNum(seqNum++);
		message.set_source(localName);
		message.set_duplicate(dupe);
	}

	private void SendAction(Message message, String dest) {
		// get destination name
		// System.out.println(message.getDupe());
		// check whether the connection has already been established
		if (!socketStatus.contains(dest)) {
			try {
				connect(dest);
			} catch (Exception e) {
				//seqNum--;
				System.out.println("Can't connect to " + dest);
				return;
			}
			// connect successfully
			System.out.println("Connect to " + dest + " is established");
			socketStatus.add(dest);
		}

		// send message
		ObjectOutputStream writer = socketSend.get(dest);
		try {
			writer.writeObject(message);
			writer.reset();
		} catch (Exception e) {
			// sending fails
			System.out.println("Sending message to " + dest + " failed");
			// update connection status
			socketStatus.remove(dest);
			// re-connect
			System.out.println("Trying to re-connect " + dest);
			try {
				connect(dest);
				// re-connect successfully, re-send message
				writer.writeObject(message);
			} catch (Exception e1) {
				// re-connect fails, give up
				System.out.println("Can't connect to " + dest);
				return;
			}
			System.out.println("Connect to " + dest + " is established");
			socketStatus.add(dest);
		}
	}

	public static void AddToRec(Message m) {
		synchronized (MessagePasser.class) {
			receiveBuffer.add(m);
			printBuffer(receiveBuffer);
		}
	}

	// debug function
	public static void printBuffer(List<Message> a) {
		for (Message m : a) {
			System.out.print(m.getData() + " ");
		}
		System.out.println();
	}

	public static String printBuffer(long[] a) {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i < a.length; i++) {
			s.append(a[i] + " ");
		}
		return s.toString();
	}

	public Message receive() {
		// check whether configuration file has been modified
		// re-read file if it is modified
		if (!configFile.CheckFile(configFileName)) {
			try {
				configFile.ReadFile(configFileName);
			} catch (IOException e) {
				System.out.println("Error! Can't access to file "
						+ configFileName);
			}
		}

		synchronized (MessagePasser.class) {
			if (receiveBuffer.size() == 0) {
				return null;
			} else {
				return receiveBuffer.remove(0);
			}
		}
	}

	public static int CheckRule(Message m, ArrayList<Rule> rules) {
		for (Rule rule : rules) {
			if (rule.MatchRule(m)) {
				if (rule.action.equals("drop")) {
					return drop;
				} else if (rule.action.equals("duplicate")) {
					return duplicate;
				} else if (rule.action.equals("delay")) {
					return delay;
				}
			}
		}
		return -1;
	}

	public void multCast(String grpName, String kind, boolean log, Object data) {
		
	}
}
