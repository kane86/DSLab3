package message;

import java.util.Arrays;
import java.util.Comparator;

@SuppressWarnings("serial")
public class TimeStampedMessage extends Message {
	private long[] timeVector;
	
	public TimeStampedMessage(String dest, String kind, boolean log, Object data, long[] timeVector) {
		super(dest, kind, log, data);
		this.timeVector = timeVector.clone();
	}
	
	@Override
	public long[] GetTimeStamp() {
		return timeVector;
	}

	@Override
	public String toString() {
		return "TimeStampedMessage [" + Arrays.toString(timeVector) + ", " + super.toString() + "]";
		
	}
    
    

}

