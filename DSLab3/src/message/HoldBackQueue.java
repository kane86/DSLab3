package message;

import java.util.ArrayList;
import java.util.HashMap;


public class HoldBackQueue {
	private ArrayList<HoldBackQueueNode> nodes = null;
	
	public HoldBackQueue()
	{
		nodes = new ArrayList<HoldBackQueueNode>();
		return;
	}
	
	public HoldBackQueueNode getNode(MultCastMessage msg, HashMap<String, Integer> allNodeIndex)
	{		
		HoldBackQueueNode retNode = null;
		
		for (HoldBackQueueNode iterNode : nodes) {
			if ((iterNode.getMultCastMessage().compareOrigin(msg, allNodeIndex)) == 0) {
				retNode = iterNode;
				break;
			}
		}
		
		return retNode;
	}
	
	public HoldBackQueueNode getNodeByTimestamp(int vectorIndex, long localVectorValue)
	{
		System.out.println("[DBG_HBQUEUE]: index: " + vectorIndex + ", Value: " + localVectorValue);
		HoldBackQueueNode retNode = null;

		for (HoldBackQueueNode iterNode : nodes) {
			if (iterNode.getMultCastMessage().getMultCastTimeStamp()[vectorIndex] == localVectorValue) {
				retNode = iterNode;
				break;
			}
		}

		return retNode;	
	}
	
	/*
	public HoldBackQueueNode getFirstPendingMessage(String msgOrigin, int vectorIndex)
	{
		HoldBackQueueNode retNode = null;
		long vectorValue = -1;

		for (HoldBackQueueNode iterNode : nodes) {
			if (iterNode.getMultCastMessage().getMultCastTimeStamp()[vectorIndex] > vectorValue) {
				retNode = iterNode;
				vectorValue = iterNode.getMultCastMessage().getMultCastTimeStamp()[vectorIndex];
			}
		}

		return retNode;
		
	}
	*/

	public HoldBackQueueNode getFirstPendingMessage(String msgOrigin, int vectorIndex)
	{
		HoldBackQueueNode retNode = null;
		long vectorValue = 100;

		for (HoldBackQueueNode iterNode : nodes) {
			if (iterNode.getMultCastMessage().getMultCastTimeStamp()[vectorIndex] < vectorValue) {
				retNode = iterNode;
				vectorValue = iterNode.getMultCastMessage().getMultCastTimeStamp()[vectorIndex];
			}
		}

		return retNode;
	}
	
	public HoldBackQueueNode getNextPendingMessage(String msgOrigin, int vectorIndex, long prevValue)
	{
		HoldBackQueueNode retNode = null;
		long vectorValue = 100;

		for (HoldBackQueueNode iterNode : nodes) {
			if (iterNode.getMultCastMessage().getMultCastTimeStamp()[vectorIndex] < vectorValue) {
				
				if (iterNode.getMultCastMessage().getMultCastTimeStamp()[vectorIndex] > prevValue) {
					retNode = iterNode;
					vectorValue = iterNode.getMultCastMessage().getMultCastTimeStamp()[vectorIndex];
				}
			}
		}

		return retNode;
	}

	public void addNewNode(HoldBackQueueNode newNode)
	{
		nodes.add(newNode);
	}
	
	public void remNode(HoldBackQueueNode oldNode)
	{
		nodes.remove(oldNode);
	}
	
	public ArrayList<HoldBackQueueNode> getAllMsgs() {
		return nodes;
	}
}
