package configData;

import java.util.Map;

import message.Message;

public class Rule {
	public String action;
	public String src;
	public String dest;
	public String kind;
	public int seqNum;
	public boolean dupe;
	private boolean hasDupe = false;
	
	public Rule(Map<String, Object> map) {
		action = (String)map.get("action");
		src = (String)map.get("src");
		dest = (String)map.get("dest");
		kind = (String)map.get("kind");
		if (map.containsKey("duplicate")) {
			dupe = (Boolean) map.get("duplicate");
			hasDupe = true;
		} 
		if (map.containsKey("seqNum")) {
			seqNum = (Integer) map.get("seqNum");
		} else {
			seqNum = -1;
		}
	}
	
	public boolean MatchRule(Message m) {
		if ((src == null || src.equals(m.getSource())) && 
			(dest == null || dest.equals(m.getDest())) &&
			(kind == null || kind.equals(m.getKind())) &&
			(!hasDupe || dupe == m.getDupe()) &&
			(seqNum == -1 || seqNum == m.getSeqNum())) {
			return true;
		} else {
			return false;
		}
	}
}
