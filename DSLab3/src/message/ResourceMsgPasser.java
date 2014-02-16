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
    private HashMap<String, VotingSet> votingSets;
    private HashMap<String, Resource> resources;
    private Thread receiveThread;
    private ResReqQueue reqQueue= null;
    private ArrayList<Resource> receiveQueue= null;
	public ResourceMsgPasser(String configuration_filename, String local_name) throws IOException {
		
		msgPass = new MessagePasser(configuration_filename, local_name);
		
		// create the groups
		votingSets = new HashMap<String, VotingSet>();	
		
		/* TODO: Populate the Voting Sets */
		
		resources = new HashMap<String, Resource>();
		reqQueue = new ResReqQueue();
		receiveQueue = new ArrayList<Resource>();
		
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
					
				case Release:
					processRelMsg(rcvdMsg);
					break;
					
				default:
					System.out.println("[DBG_RUN]: Invalid MsgType");
				}
			}
		}
	}

	public void lock(String resourceName) {
		/* send a request to the voting set */
		String grpName = null;
		Resource resource = resources.get(resourceName);
		ResReqQueueNode queueNode = reqQueue.getNode(resourceName);
		VotingSet votingSet = null;
		
		/* TODO: After parsing code, get Voting Set */
		
		if (resource == null) {
			/* Invalid resource */
			System.out.println("[DBG_LOCK]: Invalid input: " + resourceName);
			return;
		}
		
		if ((resource.isResHeld() == true) && (resource.getCurOwner().equals(msgPass.GetLocalName()))) {
			/* Redundant request */
			System.out.println("[DBG_LOCK]: Resource is already with you");
			return;
		}
		
		if (queueNode == null) {
			queueNode = new ResReqQueueNode(resource); 
			reqQueue.addNewNode(queueNode);
		} else {
			System.out.println("[DBG_LOCK]: There is already a pending request");
		}
		
		resource.setState(ResourceState.WANTED);
		multCastReq(votingSet, resourceName);
	}
	
	public String receive() {
		
		System.out.println("[DBG_RescMsgPasser]: Receive");
		Resource resource = null;
		String resourceName = null;

		if (this.receiveQueue.size() > 0) {
			resource = this.receiveQueue.remove(0);
			resourceName = resource.getName();
		}

		return resourceName;
	}
	
	public void release(String resourceName) {
		
		Resource resource = resources.get(resourceName);
		VotingSet votingSet = null;

		/* TODO: After parsing code, get Voting Set */

		if (resource == null) {
			/* Invalid resource */
			System.out.println("[DBG_LOCK]: Invalid input: " + resourceName);
			return;
		}
		

		resource.setState(ResourceState.RELEASED);
		multCastRel(votingSet, resourceName);
	}
	
	private void processReqMsg(VotingMessage rcvdMsg) {
		
		/* Received a resource request message */
		Resource resource;
		String resName;
		String senderName;
		
		senderName = rcvdMsg.getSource();
		resName = rcvdMsg.getResName();
		resource = resources.get(resName);
		
		if (resource == null) {
			System.out.println("[DBG_ProcReqMsg]: Resource does not exist");
			return;
		}
		
		if ((resource.isResHeld() == true) ||  (resource.getVoted() == true)) {
			/* enqueue the request */
			resource.addWaitingNode(rcvdMsg.getOrigin());
		} else {
			/* Send reply to request sender */
			uniCastAck(senderName, resName);
		}
	}

	private void processAckMsg(VotingMessage rcvdMsg) {
		/* Received a resource request message */
		Resource resource;
		String resName;
		String senderName;
		ResReqQueueNode queueNode;
		
		senderName = rcvdMsg.getSource();
		resName = rcvdMsg.getResName();
		resource = resources.get(resName);
		queueNode = this.reqQueue.getNode(resName);
		
		if (queueNode == null) {
			/* No Pending request sent from my side */
			System.out.println("[DBG_ProcessAckMsg]: Received non " +
							   "matching request for resource: " + resName);
			return;
		}
		
		/* There is a corresponding message in holdback queue
		 * Map this Ack to that message */
		queueNode.addAckStatus(senderName, true);
		/* Lets verify if any ack is pending on this node */
		if (queueNode.getUnAckCount() == 0) {
			/* Nothing unAcked left lets deliver the message */
			System.out.println("[DBG_multCast]: processRcvdMessage, unAck count ZERO for resource: " + resName);
			reqQueue.remNode(queueNode);
			deliverResource(resource);
		}
	}

	private void processRelMsg(VotingMessage rcvdMsg) {
		/* Received a resource request message */
		Resource resource;
		String resName;
		String senderName;
		String waitingNode;
		
		senderName = rcvdMsg.getSource();
		resName = rcvdMsg.getResName();
		resource = resources.get(resName);
		
		if (resource == null) {
			System.out.println("[DBG_ProcReqMsg]: Resource does not exist");
			return;
		}
		
		waitingNode = resource.getWaitingNode();
		
		if (waitingNode == null) {
			resource.setVoted(false);
		} else {
			/* Send reply to request sender */
			uniCastAck(waitingNode, resName);
		}
	}

	private void deliverResource(Resource resource)
	{
		System.out.println("[DBG_ResMsgPasser]: Deliver Resource: " + resource.toString());
		this.receiveQueue.add(resource);
	}
	
	private void multCastReq(VotingSet votingSet, String resName) {		
		ArrayList<String> members = votingSet.getMembers();
		if (members == null) {
			System.out.println("[ERR_multCast]: Empty Group");
			return;
		}
		System.out.println("[DBG_multCast]: Enter: grpName: " + votingSet.getName());

		for (String dest : members) {
			VotingMessage votingMsg = new VotingMessage(msgPass.GetLocalName(), votingSet.getName(), resName, dest, 
														"Resource", false, null, msgPass.GetClock().GetTimeStamp());
			votingMsg.setVotingMsgType(VotingMessageType.Req);
			msgPass.GetClock().Update();
			msgPass.send(votingMsg);
		}
	}
	
	private void multCastRel(VotingSet votingSet, String resName) {		
		ArrayList<String> members = votingSet.getMembers();
		if (members == null) {
			System.out.println("[ERR_multCast]: Empty Group");
			return;
		}
		System.out.println("[DBG_multCast]: Enter: grpName: " + votingSet.getName());

		for (String dest : members) {
			VotingMessage votingMsg = new VotingMessage(msgPass.GetLocalName(), votingSet.getName(), resName, dest, 
														"Resource", false, null, msgPass.GetClock().GetTimeStamp());
			votingMsg.setVotingMsgType(VotingMessageType.Release);
			msgPass.GetClock().Update();
			msgPass.send(votingMsg);
		}
	}
	
	private void uniCastAck(String dest, String resName) {
		VotingMessage retVotingMsg = null;
		retVotingMsg = new VotingMessage(msgPass.GetLocalName(), null, resName, dest, 
										 "Resource", false, null, msgPass.GetClock().GetTimeStamp());
		retVotingMsg.setVotingMsgType(VotingMessageType.Ack);
		msgPass.GetClock().Update();
		msgPass.send(retVotingMsg);
	}
}