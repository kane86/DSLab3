package message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HoldBackQueueNode {
	
	MultCastMessage msg;
	public HashMap<String, Boolean> ackStatus;
	
	public HoldBackQueueNode(MultCastMessage inputMsg) {
		this.msg = inputMsg;
		ackStatus = new HashMap<String, Boolean>();
		return;
	}
	
	public HoldBackQueueNode(MultCastMessage inputMsg, List<String> members) {
		this.msg = inputMsg;
		ackStatus = new HashMap<String, Boolean>();	
		for (String dest : members) {
			this.addAckStatus(dest, false);
		}
	
		return;
	}
	
	public MultCastMessage getMultCastMessage()
	{	
		return this.msg;
	}
	
	public void setMultCastMessage(MultCastMessage inputMsg)
	{	
		this.msg = inputMsg;
		return;
	}
	
	public void addAckStatus(String key, Boolean Value) {
		ackStatus.put(key, Value);
	}
	
	public Boolean getAckStatus(String key) {
		return ackStatus.get(key);
	}
	
	public int getUnAckCount() {
		int unAckCount = 0;

		for (Map.Entry<String, Boolean> entry : ackStatus.entrySet()) {
			if (entry.getValue() == false)
				unAckCount++;
		}

		return unAckCount;
	}
}
