package message;

import java.util.Comparator;

public class msgCompara implements Comparator<Message> {
	public int compare (Message msg1, Message msg2) {
		long[] timeVec1 = msg1.GetTimeStamp();
		long[] timeVec2 = msg2.GetTimeStamp();
		if (timeVec1 == null || timeVec2 == null) {
			return 0;
		}
		int vecLen = timeVec1.length;
		long[] dif = new long[vecLen];
		for (int i = 0; i < vecLen; ++i) {
			dif[i] = timeVec1[i] - timeVec2[i];
			if (dif[i] < 0) {
				return -1;
			}
			if (dif[i] > 0){
				return 1;
			}
		}

		return 0;   
	}
}

