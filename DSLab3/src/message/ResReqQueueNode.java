package message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResReqQueueNode {

	Resource resource;
	public HashMap<String, Boolean> ackStatus;

	public ResReqQueueNode(Resource inputResource) {
		this.resource = inputResource;
		ackStatus = new HashMap<String, Boolean>();
		return;
	}

	public ResReqQueueNode(Resource inputResource, List<String> members) {
		this.resource = inputResource;
		ackStatus = new HashMap<String, Boolean>();	
		for (String dest : members) {
			this.addAckStatus(dest, false);
		}

		return;
	}

	public Resource getResource()
	{	
		return this.resource;
	}

	public void setResource(Resource resource)
	{	
		this.resource = resource;
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