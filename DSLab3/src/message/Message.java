package message;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Message implements Serializable {
	private String dest = null;
	private String kind = null;
	private Object data = null;
	public String source = null;
	private int seqNum = 0;
	private boolean dupe = false;
	private boolean log = false;
	
	public Message(String dest, String kind, boolean log, Object data) {
		this.dest = dest;
		this.kind = kind;
		this.log = log;
		this.data = data;
	}
	
	public void set_source(String source) {
		this.source = source;
	}
	public void set_seqNum(int sequenceNumber) {
		this.seqNum = sequenceNumber;
	}
	public void set_duplicate(boolean dupe) {
		this.dupe = dupe;
	}
	public void set_log(boolean log) {
		this.log = log;
	}
	
	public boolean get_log() {
		return this.log;
	}
	
	public String getDest() {
		return this.dest;
	}
	
	public String getKind() {
		return this.kind;
	}
	
	public Object getData() {
		return this.data;
	}
	
	public String getSource() {
		return this.source;
	}
	
	public int getSeqNum() {
		return this.seqNum;
	}
	
	public boolean getDupe() {
		return this.dupe;
	}
	
	// overwrite by TimeStampedMessage
	public long[] GetTimeStamp() {
		return null;
	}
	
	// overwrite by MultiCastMessage
	public int getMultCastSeqNum() {
		return -1;
	}
	
	// overwrite by MultiCastMessage
	public void setMultCastSeqNum(int multCastSeqNum) {
	}
	
	// overwrite by MultiCastMessage
	public String getGrpName() {
		return null;
	}

	@Override
	public String toString() {
		return "dest=" + dest + ", kind=" + kind + ", data=" + data
				+ ", source=" + source + ", seqNum=" + seqNum + ", dupe="
				+ dupe + ", log=" + log;
	}
	
	
	
	
}
