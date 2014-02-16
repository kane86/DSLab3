package message;

import java.util.ArrayList;

public class Resource {
	String name;
	String curOwner;
	ResourceState state = ResourceState.RELEASED;
	boolean voted = false;
	ArrayList<String> waitingNodes;

	public Resource(String name) {
		this.name = name;
		waitingNodes = new ArrayList<String>();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
		return;
	}

	public String getCurOwner() {
		return this.curOwner;
	}

	public void setCurOwner(String owner) {
		this.curOwner = owner;
		return;
	}

	public ResourceState getState() {
		return this.state;
	}

	public void setState(ResourceState state) {
		this.state = state;
		return;
	}

	public boolean getVoted() {
		return this.voted;
	}

	public void setVoted(boolean voted) {
		this.voted = voted;
		return;
	}

	public boolean isResHeld() {
		boolean ret = false;

		if (this.state == ResourceState.HELD)
			ret = true;

		return ret;
	}

	public boolean isResFree() {
		boolean ret = false;

		if (this.state == ResourceState.RELEASED)
			ret = true;

		return ret;
	}

	public void addWaitingNode(String nodeName)
	{
		if (isNodeWaiting(nodeName) == true) {
			System.out.println("[DBS_RESOURCE]: AddWatiNode, Node: " + nodeName + " ,already in the list");
			return;
		}

		waitingNodes.add(nodeName);
	}

	public String getWaitingNode()
	{
		String retNode = null;

		if (waitingNodes.size() > 0) {
			retNode = waitingNodes.remove(0);
		}

		return retNode;
	}

	private boolean isNodeWaiting(String nodeName) {

		boolean nodeWaiting = false;

		for (String iter: waitingNodes) {
			if (iter.equals(nodeName)) {			
				nodeWaiting = true;
				break;
			}				
		}

		return nodeWaiting;
	}
	
	public String toString() {
		
		String retString;
	  	
		retString = "[RESOURCE]: {name: "+name+"}";
		retString = retString + "{owner: "+curOwner+"}";
		retString = retString + "{state: "+state+"}";
		retString = retString + "{voted: "+voted+"}";
		retString = retString + "{Members: [";
		for (String iter : waitingNodes) {
			retString = retString + "node: " + iter + "], ["; 
			
		}
		
		retString = retString + "]}";
		return retString;
	}
}