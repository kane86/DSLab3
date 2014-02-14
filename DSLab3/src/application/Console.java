package application;

import java.io.IOException;

import message.*;


public class Console {
	private MultiCastMsgPasser passer;

	public Console(String configuration_filename, String local_name) {
		try {
			passer = new MultiCastMsgPasser(configuration_filename, local_name);
		} catch (IOException e) {
			System.out.println("Error! Can't start console");
		}
	}
	
	public void Display() {
		System.out.print(passer.msgPass.GetLocalName() + ">> ");
	}

	public void Action(String s) {
		String[] ss = s.split(" ");
		if (ss.length <= 0) {
			System.out.println();
		} else if (ss[0].equals("send")) {
			if (ss.length < 4) {
				System.out.println("Illegal Message!");
			}
			boolean log = Boolean.getBoolean(ss[3]);
			Message m = new TimeStampedMessage(ss[1], ss[2], log, s.substring(ss[0].length()
					+ ss[1].length() + ss[2].length() +ss[3].length() + 4), passer.msgPass.GetClock().GetTimeStamp());
			passer.msgPass.GetClock().Update();
			passer.msgPass.send(m);
		} else if (ss[0].equals("receive")) {
			TimeStampedMessage m = (TimeStampedMessage)passer.msgPass.receive();
			if (m != null) {
				System.out.println(m.getSeqNum() + " " + m.getSource() + ": " + m.getData() + " " + MessagePasser.printBuffer(m.GetTimeStamp()));
			}
		}
	}
}
