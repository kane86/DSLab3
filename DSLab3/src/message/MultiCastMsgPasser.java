package message;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import configData.IpPort;

import clockService.VectorClock;

public class MultiCastMsgPasser implements Runnable {
	
	public MessagePasser msgPass;
    HashMap<String, Group> groupList;
    private ArrayList<MultCastMessage> rcvQueue;
    private HashMap<String, Integer> allNodeIndex;
    Thread receiveThread;
    
	public MultiCastMsgPasser(String configuration_filename, String local_name) throws IOException {
		
		msgPass = new MessagePasser(configuration_filename, local_name);
		
		// record the index of all nodes in the time stamp vector
		allNodeIndex = new HashMap<String, Integer>();
		for (Map.Entry<String, IpPort> entry : msgPass.configFile.nodeList.entrySet()) {
			allNodeIndex.put(entry.getKey(), entry.getValue().index);
		}
		
		// create the groups
		groupList = new HashMap<String, Group>();	
		HashMap<String, List<String>> groups = msgPass.configFile.getGroups();
		for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
			Group oneGrp = new Group(entry.getKey(), entry.getValue(), msgPass.configFile.configList.size(), msgPass.configFile.nodeList.get(local_name).index);
			groupList.put(entry.getKey(), oneGrp);
			System.out.println("[DBG]: Group: " + oneGrp.toString());
		}
		
		rcvQueue = new ArrayList<MultCastMessage>();
		
		/* Receive thread */
		receiveThread = new Thread(this, "Multicast Message receive thread");
		receiveThread.start();
	}
	
	/* Start the receive thread which will continuously receive
	 * messages from MessagePasser.*/
	public void run() {
		MultCastMessage rcvdMsg;
		MultiCastMessageType rcvdMsgType;
		int valStatus = 0;
		System.out.println("[DBG] Enter Thread");
		while (true) {
			rcvdMsg = (MultCastMessage)(msgPass.receive());
			
			if (rcvdMsg != null) {	
				rcvdMsgType = rcvdMsg.getMultCastMsgType();
				
				if (rcvdMsgType == MultiCastMessageType.Nack) {
					processNackMessage(rcvdMsg);
				} else {
					/* Process received Message */
					valStatus = validateReceive(rcvdMsg);
					if (valStatus == -1) {
						/* Received an out of sequence message */
						System.out.println("[RCVDMSG]: NotValid: " + rcvdMsg.toString());
						sendNacks(rcvdMsg);
					}
					processRcvdMessage(rcvdMsg, valStatus);
				}
			}
		}
	}
	
	public void multCast (String grpName, String kind, boolean log, Object data) {
		
		Group group = groupList.get(grpName);
		ArrayList<String> members = group.getMembers();
		if (members == null) {
			System.out.println("[ERR_multCast]: Empty Group");
			return;
		}
		System.out.println("[DBG_multCast]: Enter: grpName: " + grpName);
		// increase the multicast sequence number
		int seqNum = group.multCastSeqNum.incrementAndGet();
		long[] multCastTimeStamp = group.GetClock().GetTimeStamp();
		multCastTimeStamp[allNodeIndex.get(msgPass.localName)]++;
		
		for (String dest : members) {
    		MultCastMessage multCastMsg = new MultCastMessage(msgPass.GetLocalName(), msgPass.GetLocalName(), dest, kind, log, data, msgPass.GetClock().GetTimeStamp(), multCastTimeStamp);
	    	multCastMsg.setMultCastSeqNum(seqNum);
	    	multCastMsg.setMultCastMsgType(MultiCastMessageType.Data);
	    	multCastMsg.setGrpName(grpName);
	 	
		    msgPass.GetClock().Update();
		    msgPass.send(multCastMsg);
		}
		
	}
	
	public MultCastMessage receive() {
		System.out.println("[DBG_multicast]: Receive");
		MultCastMessage recvMessage = null;

		if (rcvQueue.size() > 0) {
			recvMessage = this.rcvQueue.remove(0);
		}
		
		return recvMessage;
	}
	
	private void sendNacks(MultCastMessage rcvdMsg) {
		
		MultiCastMessageType rcvdMsgType = rcvdMsg.getMultCastMsgType();
		HoldBackQueueNode hbNode = null;
		String grpName =  rcvdMsg.getGrpName();
		Group group = groupList.get(rcvdMsg.getGrpName());
		List<String> members = group.getMembers();
		String msgSource = rcvdMsg.getSource();
		String msgOrigin = rcvdMsg.getOrigin();
		MultCastMessage ackMsg;
		MultCastMessage delMsg;
		MultCastMessage nackedMsg;
		Boolean ackStatus;
		int vectorIndex = allNodeIndex.get(msgOrigin);
		long localVectorValue = 0;
		//localVectorValue = group.getVectorValue(allNodeIndex, msgOrigin);
		
		HoldBackQueue holdBackQueue = group.getHoldBackQueue();	
		hbNode = holdBackQueue.getNode(rcvdMsg, allNodeIndex);
		//hbNode = holdBackQueue.getNodeByTimestamp(vectorIndex, localVectorValue);
		hbNode = holdBackQueue.getFirstPendingMessage(msgOrigin, vectorIndex);

		while (true) {
			if (hbNode == null) {
				System.out.println("[DBG_SENDNACKS]: Missing node NOT in Holdback Queue");

				/* Send Nack to all the group members */
				/*
			for (String dest : members) {
				long[] multCastTimeStamp = group.GetClock().GetTimeStamp();
				nackedMsg = new MultCastMessage(rcvdMsg.getOrigin(), msgPass.GetLocalName(), dest, 
												"Nack", false, null, msgPass.GetClock().GetTimeStamp(), 
												 multCastTimeStamp);
				unicastNack(dest, nackedMsg);

			}
				 */
				break;
				
			} else {
				for (String dest : members) {
					ackStatus = hbNode.getAckStatus(dest);
					if (ackStatus == false) {
						nackedMsg = hbNode.getMultCastMessage();
						unicastNack(dest, nackedMsg);
					}
				}
				localVectorValue = hbNode.getMultCastMessage().getMultCastTimeStamp()[vectorIndex];	
				hbNode = holdBackQueue.getNextPendingMessage(msgOrigin, vectorIndex, localVectorValue);
			}
		}
		
		return;
	}
	
	private void processRcvdMessage(MultCastMessage rcvdMsg, int valStatus) {
		
		MultiCastMessageType rcvdMsgType = rcvdMsg.getMultCastMsgType();
		HoldBackQueueNode hbNode = null;
		Group group = groupList.get(rcvdMsg.getGrpName());
		List<String> members = group.getMembers();
		String msgSource = rcvdMsg.getSource();
		String msgOrigin = rcvdMsg.getOrigin();
		MultCastMessage ackMsg;
		MultCastMessage delMsg;
	
		HoldBackQueue holdBackQueue = group.getHoldBackQueue();	
		hbNode = holdBackQueue.getNode(rcvdMsg, allNodeIndex);
			
		System.out.println("[DBG_multCast]: Rcvd Msg: " + rcvdMsg.toString());
		
		/* Received an Ack Message */
		if (hbNode != null) {
			System.out.println("[DBG_multCast]: hbNode found ");
			
			/* Lets check if we received a data packet after ack */
			if (hbNode.getMultCastMessage().getMultCastMsgType() == MultiCastMessageType.Ack) {
				
				if (rcvdMsg.getMultCastMsgType() == MultiCastMessageType.Data) {
					
					/* Ok so we have a data packet after ack, letc copy data
					 * packet's timestamp.
					 */ 
					hbNode.getMultCastMessage().setMultCastTimeStamp(rcvdMsg.getMultCastTimeStamp());
				}
			}
			
			/* There is a corresponding message in holdback queue
			 * Map this Ack to that message */
			hbNode.addAckStatus(msgSource, true);
			
			/* Lets verify if any ack is pending on this node */
			if (hbNode.getUnAckCount() == 0) {
				/* Nothing unAcked left lets deliver the message */
				System.out.println("[DBG_multCast]: processRcvdMessage, unAck count ZERO for msg: " + hbNode.getMultCastMessage().toString());
				causalOrdering();
			}
			
		} else {
					
			/*
			if (valStatus == 1) {
				System.out.println("[DBG_multCast]: Rcvd Older Msg: " + rcvdMsg.toString());
				return;
			}
			*/
			
			System.out.println("[DBG_multCast]: Create new hbNode ");
			/* Create a new node */
			hbNode = new HoldBackQueueNode(rcvdMsg, members);
			
			hbNode.addAckStatus(msgSource, true);
			hbNode.addAckStatus(msgPass.GetLocalName(), true);
			
			holdBackQueue.addNewNode(hbNode);
			
			if (rcvdMsgType == MultiCastMessageType.Data && (msgSource.equals(msgPass.GetLocalName()) == true)) {
				System.out.println("[DBG_multCast]: NOT sending bcast, DataType:" + rcvdMsgType + 
						   ", MsgSource: " + msgSource + ", MyName: " + msgPass.GetLocalName());
			} else {
				/* Broadcast an Ack message */
				System.out.println("[DBG_multCast]: sending bcast ");
				broadcastAck(rcvdMsg);
			}
			
			/* Lets verify if any ack is pending on this node */
			if (hbNode.getUnAckCount() == 0) {
				/* Nothing unAcked left lets deliver the message */
				System.out.println("[DBG_multCast]: processRcvdMessage, unAck count ZERO for msg: " + hbNode.getMultCastMessage().toString());
				causalOrdering();
			}
			
			/*
			if (rcvdMsgType == MultiCastMessageType.Data && (msgSource.equals(msgPass.GetLocalName()) == false)) {
				System.out.println("[DBG_multCast]: sending bcast ");
				broadcastAck(rcvdMsg);
			} else {
				System.out.println("[DBG_multCast]: NOT sending bcast, DataType:" + rcvdMsgType + 
								   ", MsgSource: " + msgSource + ", MyName: " + msgPass.GetLocalName());
			}
			*/
		}
	}
	
	private void broadcastAck(MultCastMessage rcvdMsg) {
		
		int seqNum = rcvdMsg.getMultCastSeqNum();
		String kind = rcvdMsg.getKind();
		boolean log = rcvdMsg.get_log();
		Object data = rcvdMsg.getData();
		String grpName = rcvdMsg.getGrpName();
		
		Group group = groupList.get(rcvdMsg.getGrpName());
		List<String> members = group.getMembers();
		long[] timeVector = msgPass.GetClock().GetTimeStamp();
		
		if (members == null) {
			return;
		}
		
		//long[] multCastTimeStamp = group.GetClock().GetTimeStamp();
		long[] multCastTimeStamp = rcvdMsg.getMultCastTimeStamp().clone();
		System.out.println("[DBG_BROADCAST] rcvdMsg Msg: " + rcvdMsg.toString());
		
		for (String dest : members) {
    		MultCastMessage multCastMsg = new MultCastMessage(rcvdMsg.getOrigin(), msgPass.GetLocalName(), dest, kind, log, data, timeVector, multCastTimeStamp);

    		if (dest.equals(msgPass.GetLocalName())) {
    			System.out.println("[DBG], Do not ACK Yourself");
    			continue;	
    		}
    		
	    	multCastMsg.setMultCastSeqNum(seqNum);
	    	multCastMsg.setMultCastMsgType(MultiCastMessageType.Ack);
	    	multCastMsg.setGrpName(grpName);
	    	
	    	System.out.println("[DBG_BROADCAST] Bcast: dest: " + dest + ", Msg: " + multCastMsg.toString());	
	    	
	    	msgPass.GetClock().Update();
		    msgPass.send(multCastMsg);
		}
	}

	private void unicastAck(String dest, MultCastMessage rcvdMsg) {

		int seqNum = rcvdMsg.getMultCastSeqNum();
		String kind = rcvdMsg.getKind();
		boolean log = rcvdMsg.get_log();
		Object data = rcvdMsg.getData();
		String grpName = rcvdMsg.getGrpName();

		Group group = groupList.get(rcvdMsg.getGrpName());
		long[] timeVector = msgPass.GetClock().GetTimeStamp();
		long[] multCastTimeStamp = rcvdMsg.getMultCastTimeStamp().clone();
		System.out.println("[DBG_BROADCAST] rcvdMsg Msg: " + rcvdMsg.toString());

		MultCastMessage multCastMsg = new MultCastMessage(rcvdMsg.getOrigin(), msgPass.GetLocalName(), dest, kind, log, data, timeVector, multCastTimeStamp);
		multCastMsg.setMultCastSeqNum(seqNum);
		multCastMsg.setMultCastMsgType(MultiCastMessageType.Ack);
		multCastMsg.setGrpName(grpName);

		System.out.println("[DBG_UNICAST] UNICAST Msg: " + multCastMsg.toString());	

		msgPass.GetClock().Update();
		msgPass.send(multCastMsg);
	}
	
	private void unicastNack(String dest, MultCastMessage rcvdMsg) {
		
		int seqNum = rcvdMsg.getMultCastSeqNum();
		String kind = rcvdMsg.getKind();
		boolean log = rcvdMsg.get_log();
		Object data = rcvdMsg.getData();
		String grpName = rcvdMsg.getGrpName();
		Group group = groupList.get(rcvdMsg.getGrpName());
		long[] timeVector = msgPass.GetClock().GetTimeStamp();
		long[] multCastTimeStamp = rcvdMsg.getMultCastTimeStamp().clone();

		MultCastMessage multCastMsg = new MultCastMessage(rcvdMsg.getOrigin(), msgPass.GetLocalName(), dest, kind, log, data, timeVector, multCastTimeStamp);
		multCastMsg.setMultCastSeqNum(seqNum);
		multCastMsg.setMultCastMsgType(MultiCastMessageType.Nack);
		multCastMsg.setGrpName(grpName);

		System.out.println("[DBG_UNICAST] UNICASTNACK Dest: " + dest +", Msg: " + multCastMsg.toString());	

		msgPass.GetClock().Update();
		msgPass.send(multCastMsg);
	}

	private int validateReceive(MultCastMessage rcvdMsg) 
	{
		int ret = 0;
		String grpName =  rcvdMsg.getGrpName(); 
		Group group = groupList.get(grpName);
		String origin = rcvdMsg.getOrigin();
		String source = rcvdMsg.getSource();
		int vectorIndex = allNodeIndex.get(origin);
		long localVectorValue = 0;
		long rcvdVectorValue = rcvdMsg.getMultCastTimeStamp()[vectorIndex];
		localVectorValue = group.getVectorValue(allNodeIndex, origin);

		if (rcvdVectorValue == (localVectorValue + 1)) {
			ret = 0;
		} else if (rcvdVectorValue > (localVectorValue + 1)) {
			ret = -1;
		} else {
			ret = 1;
		}
		
		System.out.println("[DBG_ValidateRcv]: msg: " + rcvdMsg.toString() + ", ret: " + ret);
		
		return ret;
	}
	
	private void deliverMessage(MultCastMessage rcvdMsg)
	{
		System.out.println("[DBG_multcastPasser]: Deliver Msg: " + rcvdMsg.toString());
		HoldBackQueueNode hbNode = null;
		HoldBackQueue holdBackQueue = null;
		Group group = groupList.get(rcvdMsg.getGrpName());
		
		holdBackQueue = group.getHoldBackQueue();
		hbNode = holdBackQueue.getNode(rcvdMsg, allNodeIndex);
		holdBackQueue.remNode(hbNode);
		
		this.rcvQueue.add(rcvdMsg);
	}
	
	private void processNackMessage(MultCastMessage rcvdMsg)
	{
		String grpName =  rcvdMsg.getGrpName(); 
		Group group = groupList.get(grpName);
		String origin = rcvdMsg.getOrigin();
		String source = rcvdMsg.getSource();
		int vectorIndex = allNodeIndex.get(origin);
		long localVectorValue = 0;
		long rcvdVectorValue = rcvdMsg.getMultCastTimeStamp()[vectorIndex];
		
		System.out.println("[DBG_ProcessNackMsg]: Rcvd Nack from: " + rcvdMsg.getSource());
		/* Check if i have already sent it */
		localVectorValue = group.getVectorValue(allNodeIndex, origin);
		
		if (localVectorValue >= rcvdVectorValue) {
			/* Lets send a unicast Ack back to the sender */
			unicastAck(source, rcvdMsg);
		}
		
		return;
	}
	
	private void causalOrdering() {
		Group group = null;
		boolean moreIter = false;

		while (true) {
			moreIter = false;
			for (Map.Entry<String, Group> entry : groupList.entrySet()) {
				group = entry.getValue();
				MultCastMessage msg = group.causalOrdering(allNodeIndex, msgPass.GetLocalName());
				if (msg != null) {
					moreIter = true;
					deliverMessage(msg);
				}
			}
			
			if (moreIter == false) {
				System.out.println("[DBG_multcastPasser]: CAUSALORDERING moreIter NULL");
				break;
			}
		}
	}
}