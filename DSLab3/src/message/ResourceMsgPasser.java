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
    private ArrayList<Resource> resources;
    Thread receiveThread;
    
	public ResourceMsgPasser(String configuration_filename, String local_name) throws IOException {
		
		msgPass = new MessagePasser(configuration_filename, local_name);
		
		// create the groups
		votingSets = new HashMap<String, VotingSet>();	
		
		/* TODO: Populate the Voting Sets */
		
		resources = new ArrayList<Resource>();
		
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
				switch (rcvdMsg.getVotingMsgType()) {

				case Req:
					processReqMsg(rcvdMsg);
					break;

				case Ack:
					processAckMsg(rcvdMsg);
					break;
					
				default:
					System.out.println("[DBG_RUN]: Invalid MsgType");
				}
			}
		}
	}

	public void getResource(String resourceName) {
		/* send a request to the voting set */
		/* TODO: After parsing code */
	}
	
	private void processReqMsg(VotingMessage rcvdMsg){
	}

	private void processAckMsg(VotingMessage rcvdMsg){
	}

	private void multCast(String grpName, String resName, String kind, boolean log, Object data) {
		
		VotingSet votingSet;

		/* TODO Get Voting Set */
		votingSet = votingSets.get(grpName);
		ArrayList<String> members = votingSet.getMembers();
		if (members == null) {
			System.out.println("[ERR_multCast]: Empty Group");
			return;
		}
		System.out.println("[DBG_multCast]: Enter: grpName: " + grpName);

		for (String dest : members) {
			VotingMessage votingMsg = new VotingMessage(msgPass.GetLocalName(), grpName, resName, dest, 
														"Resource", false, null, msgPass.GetClock().GetTimeStamp());
			votingMsg.setVotingMsgType(VotingMessageType.Req);
			msgPass.GetClock().Update();
			msgPass.send(votingMsg);
		}
	}
	
	private Resource searchResource(String name) {
		Resource retResource = null;

		for (Resource iter: resources) {
			if (iter.getName().equals(name)) {
				retResource = iter;
				break;
			}
		}

		return retResource;
	}
}