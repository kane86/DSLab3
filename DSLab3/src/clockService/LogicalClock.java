package clockService;

import message.TimeStampedMessage;

public class LogicalClock extends ClockService {
	public LogicalClock() {
		super();
		clockVector = new long[1];
	}

	public void Update(TimeStampedMessage m) {
		synchronized (ClockService.class) {
			clockVector[0] = Math.max(clockVector[0], m.GetTimeStamp()[0]) + 1;
		}
	}
	
	public void Update() {
		synchronized (ClockService.class) {
			clockVector[0]++;
		}
	}
}
