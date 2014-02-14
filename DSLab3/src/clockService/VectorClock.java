package clockService;

import message.TimeStampedMessage;

public class VectorClock extends ClockService{
	private int myIndex;
	
	public VectorClock(int vectorLength, int myIndex) {
		super();
		
		this.myIndex = myIndex;
		clockVector = new long[vectorLength];
		for (int i = 0; i < vectorLength; i++) {
			clockVector[i] = 0;
		}
	}
	
	public void Update() {
		synchronized(ClockService.class) {
			clockVector[myIndex]++;
		}
	}
	
	public void UpdateByIdx(int idx) {
		synchronized(ClockService.class) {
			clockVector[idx]++;
		}
	}
	
	public void Update(TimeStampedMessage m) {
		synchronized(ClockService.class) {
			for (int i = 0; i < clockVector.length; i++) {
				if (i != myIndex) {
					clockVector[i] = Math.max(clockVector[i], m.GetTimeStamp()[i]);
				}
			}
		}
		
	}
	
	public int getMyIdx() {
		return myIndex;
	}
}
