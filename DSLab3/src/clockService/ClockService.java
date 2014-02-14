package clockService;

import message.TimeStampedMessage;

public abstract class ClockService {
	public long[] clockVector;

	public ClockService() {
	}

	public long[] GetTimeStamp() {
		return clockVector;
	}

	public void Update(TimeStampedMessage m) {

	}

	public void Update() {
		
	}
}
