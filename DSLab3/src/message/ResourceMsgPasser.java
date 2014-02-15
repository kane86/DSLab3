package message;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import configData.IpPort;

import clockService.VectorClock;

public class ResourceMsgPasser implements Runnable {
	
	public MessagePasser msgPass;
    HashMap<String, VotingSet> votingSets;
    Thread receiveThread;
    
	public ResourceMsgPasser(String configuration_filename, String local_name) throws IOException {
		
		msgPass = new MessagePasser(configuration_filename, local_name);
		
		// create the groups
		votingSets = new HashMap<String, VotingSet>();	
		
		/* TODO: Populate the Voting Sets */
		
		/* Receive thread */
		receiveThread = new Thread(this, "Multicast Message receive thread");
		receiveThread.start();
	}
	
	/* Start the receive thread which will continuously receive
	 * messages from MessagePasser.*/
	public void run() {
		VotingMessage rcvdMsg;
		VotingMessageType rcvdMsgType;
		System.out.println("[DBG] Enter Thread");
		while (true) {
			rcvdMsg = (VotingMessage)(msgPass.receive());
			if (rcvdMsg != null) {
				System.out.println("Rcvd VotingMsg: " + rcvdMsg.toString());
			}
		}
	}
}