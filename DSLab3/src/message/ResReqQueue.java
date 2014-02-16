package message;

import java.util.ArrayList;
import java.util.HashMap;


public class ResReqQueue {
	private ArrayList<ResReqQueueNode> nodes = null;

	public ResReqQueue()
	{
		nodes = new ArrayList<ResReqQueueNode>();
		return;
	}

	public ResReqQueueNode getNode(String resource)
	{		
		ResReqQueueNode retNode = null;

		for (ResReqQueueNode iterNode : nodes) {
			if ((iterNode.getResource().getName().equals(resource))) {
				retNode = iterNode;
				break;
			}
		}

		return retNode;
	}


	public void addNewNode(ResReqQueueNode newNode)
	{
		nodes.add(newNode);
	}

	public void remNode(ResReqQueueNode oldNode)
	{
		nodes.remove(oldNode);
	}

	public ArrayList<ResReqQueueNode> getAllMsgs() {
		return nodes;
	}
}