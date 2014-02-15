package message;

import java.util.HashMap;

public class VotingMessage extends TimeStampedMessage {

	private static final long serialVersionUID = -8747085586394056606L;
	private int SeqNum = 0;
	private VotingMessageType msgType = VotingMessageType.Req;
	private String msgOrigin;
	private String grpName;
	private String resName;
	
	public VotingMessage(String orginalSrc, String grpName, String resName, String dest, String kind, boolean log, Object data, long[] timeVector) {
		super(dest, kind, log, data, timeVector);
		this.msgOrigin = orginalSrc;
		this.grpName = grpName;
		this.resName = resName;
	}
	
	public VotingMessageType VotingMsgType() {
		return msgType;
	}

	public void setVotingMsgType(VotingMessageType inpMsgType) {
		this.msgType = inpMsgType;
	}
	
	public VotingMessageType getVotingMsgType() {
		return msgType;
	}
	
	public String getGrpName() {
		return grpName;
	}

	public void setGrpName(String grpName) {
		this.grpName = grpName;
	}
	
	public String getOrigin() {
		return msgOrigin;
	}

	public void setOrigin(String source) {
		this.msgOrigin = source;
	}	

	public String getResName() {
		return resName;
	}

	public void setResName(String resName) {
		this.resName = resName;
	}
	
	public String toString() {
		String retString;
		retString = "VotingMessage: grpName: [" + this.getGrpName() + "], " + " msgType: [" + this.msgType + "], Votingsender: [" + this.getOrigin() + "]";
		retString = retString + ", MsgSource: [" + getSource() +"]";
		return retString;
	}
	
}