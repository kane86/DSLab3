package message;

import java.io.IOException;
import java.util.*;

public class LogServerPasser extends MessagePasser {

	public LogServerPasser(String configuration_filename, String local_name) throws IOException  {
		super(configuration_filename, local_name);
	}

	public boolean sortMessage() {
		if (configFile.clockType.equals("vector") == true) {
			Collections.sort(receiveBuffer, new msgCompara());
		}
		return false;
	}

	public String outputMessages() {
		int len = receiveBuffer.size();
		if (len == 0) {
			return "";
		}
		
		StringBuffer strBuf = new StringBuffer();
		if (configFile.clockType.equals("vector") == false) {
			for (Message msg : receiveBuffer) {
				strBuf.append(msg.toString());
				strBuf.append("\n\n");
			}
		} else {
			Message preMsg = null;
			for (Message msg : receiveBuffer) {
				if (preMsg != null) {
					long[] timeVec1 = preMsg.GetTimeStamp();
					long[] timeVec2 = msg.GetTimeStamp();
					int vecLen = timeVec1.length;
					boolean allSmall = true;
					boolean allEqual = true;
					for (int i = 0; i < vecLen; ++i) {
						long diff = timeVec1[i] - timeVec2[i]; 
						if (diff > 0){
							allSmall = false;
							allEqual = false;
							break;
						} else if (diff < 0) {
							allEqual = false;
						}
					}

					if (allEqual == true) {
						strBuf.append(" => \n");
					} else if (allSmall == true) {
						strBuf.append(" -> \n");
					} else {
						strBuf.append(" => \n");
					}


				}
				strBuf.append(msg.toString());
				strBuf.append("\n");
				preMsg = msg;
			}
		}
		return strBuf.toString();

	}
}


