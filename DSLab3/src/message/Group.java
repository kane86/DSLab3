package message;

import clockService.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Group {
    private String grpName = "";
    private ArrayList<String> nodes;
    private HoldBackQueue holdBackQueue;
    private VectorClock timeStamp;
    AtomicInteger multCastSeqNum = new AtomicInteger(-1);
  
	public Group(String name, List<String> nodesInGrp, int totalNodeNum, int curNodeIdx) {
		grpName = name;
		nodes = new ArrayList<String>();
		for (String nodeName : nodesInGrp) {
			nodes.add(nodeName);
		}
		
		// init the queues and timstamp
		holdBackQueue = new HoldBackQueue();
		timeStamp = new VectorClock(totalNodeNum, curNodeIdx);
	}
	
	public ArrayList<String> getMembers() {
		return nodes;
	}
	
	public HoldBackQueue getHoldBackQueue() {
		return holdBackQueue;
	}
	
	public MultCastMessage causalOrdering(HashMap<String, Integer> allNodeIndex, String localName) {
		ArrayList<HoldBackQueueNode> nodes = holdBackQueue.getAllMsgs();
		for (HoldBackQueueNode node : nodes) {
			MultCastMessage msg = node.getMultCastMessage();
			long[] msgTimeStamp = msg.getMultCastTimeStamp();
			long[] myTimeStamp = timeStamp.GetTimeStamp();
			String srcName = msg.getOrigin();
			int srcIdx = allNodeIndex.get(srcName);
			int myIdx = allNodeIndex.get(localName);
			int timeStampLen = msgTimeStamp.length;
			if (node.getUnAckCount() == 0) {
				if ((srcIdx == myIdx && msgTimeStamp[srcIdx] <= myTimeStamp[srcIdx]) || 
					(msgTimeStamp[srcIdx] == myTimeStamp[srcIdx] + 1)) {
					int k = 0;
					for (k = 0; k < timeStampLen; k++) {
						if (k != srcIdx) {
							if (msgTimeStamp[k] > myTimeStamp[k]) {
								break;
							}
						}
					}
					if (k == timeStampLen) { // msg is ready to be delivered
						if (srcIdx != myIdx) {
						    timeStamp.UpdateByIdx(srcIdx);
						}
						return msg;
					}
				}
			}
		}
		return null;
	}
	
	public String toString() {
		String retString;
		retString = "GROUP: [" + grpName + "], ";
		for (String nodeName: nodes) {
			retString = retString + " node: " + nodeName + ", ";
		}
		
		return retString;
	}
	
	public VectorClock GetClock() {
		return timeStamp;
	}
	
	public long getVectorValue(HashMap<String, Integer> allNodeIndex, String localName) 
	{
		int index = allNodeIndex.get(localName);
		return timeStamp.GetTimeStamp()[index];
	}
}
