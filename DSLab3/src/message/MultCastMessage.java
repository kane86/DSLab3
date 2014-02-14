package message;

import java.util.HashMap;

public class MultCastMessage extends TimeStampedMessage {

	private static final long serialVersionUID = -8747085586394056606L;
	private int multCastSeqNum = 0;
	private MultiCastMessageType msgType = MultiCastMessageType.Data;
	private String grpName;
	private String msgOrigin;
	long[] multCastTimeVector;
	
	public MultCastMessage(String orginalSrc, String source, String dest, String kind, boolean log, Object data, long[] timeVector, long[] multCastTimeVector) {
		super(dest, kind, log, data, timeVector);
		multCastSeqNum = 0;
		this.multCastTimeVector = multCastTimeVector.clone();
		this.msgOrigin = orginalSrc;
		this.source = source;
	}

	public int getMultCastSeqNum() {
		return multCastSeqNum;
	}

	public void setMultCastSeqNum(int multCastSeqNum) {
		this.multCastSeqNum = multCastSeqNum;
	}
	
	public MultiCastMessageType getMultCastMsgType() {
		return msgType;
	}

	public void setMultCastMsgType(MultiCastMessageType inpMsgType) {
		this.msgType = inpMsgType;
	}

	public String getGrpName() {
		return grpName;
	}

	public void setGrpName(String grpName) {
		this.grpName = grpName;
	}
	
	/* override */
	public String getOrigin() {
		return msgOrigin;
	}

	public void setOrigin(String source) {
		this.msgOrigin = source;
	}
	
	
	public int compare(MultCastMessage inputMsg)
	{
		int ret = 0;
		long[] timeVec1 = this.getMultCastTimeStamp();
		long[] timeVec2 = inputMsg.getMultCastTimeStamp();
		int vecLen = timeVec1.length;

		if (inputMsg.getOrigin().equals(this.getOrigin()) &&
				(inputMsg.getGrpName().equals(this.getGrpName()))) {

			if (timeVec1 == null || timeVec2 == null) {
				return -1;
			}
			
			long[] dif = new long[vecLen];
			for (int i = 0; i < vecLen; ++i) {
				dif[i] = timeVec1[i] - timeVec2[i];
				if (dif[i] < 0) {
					ret = -1;
				}
				if (dif[i] > 0) {
					ret = 1;
				}
			}
		}
		return ret;
	}
	
	public int compareOrigin(MultCastMessage inputMsg, HashMap<String, Integer> allNodeIndex)
	{
		int ret = 0;
		long[] timeVec1 = this.getMultCastTimeStamp();
		long[] timeVec2 = inputMsg.getMultCastTimeStamp();
		String originName = inputMsg.getOrigin();
		int originIndex = allNodeIndex.get(originName);

		if (inputMsg.getOrigin().equals(this.getOrigin()) &&
				(inputMsg.getGrpName().equals(this.getGrpName()))) {

			if (timeVec1 == null || timeVec2 == null) {
				return -1;
			}
			
			//System.out.println("[MULT_MSG]: CompareOrigin: myMsg: " + this.toString() + ", inputMsg: " + inputMsg.toString());
			//System.out.println("[MULT_MSG]: OriginIndex: " + originIndex);
			if ((timeVec1[originIndex] - timeVec2[originIndex]) < 0) {
				ret = -1;
			} else if ((timeVec1[originIndex] - timeVec2[originIndex]) > 0) {
				ret = 1;
			}
		}
		
		return ret;
	}
	
	
	public String toString() {
		String retString;
		retString = "MultCastMessage: grpName: [" + this.grpName + "], " + ", seqNum: " + this.multCastSeqNum + " msgType: " + this.msgType + ", Multicastsender: " + this.getOrigin();
		retString = retString + ", Timestamp:[";
		
		for (long x: multCastTimeVector)
			retString +=  x + ",";
		
		retString = retString + "]";
		retString = retString + ", MsgSource: [" + getSource() +"]";
		return retString;
	}
	
	public long[] getMultCastTimeStamp() {
		return multCastTimeVector;
	}
	
	public void setMultCastTimeStamp(long[] inputVector) {
		multCastTimeVector = inputVector.clone();
		return;
	}
}
