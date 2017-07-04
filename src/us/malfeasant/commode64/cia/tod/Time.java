package us.malfeasant.commode64.cia.tod;

class Time {
	final DigitSet timeDigits = new DigitSet();
	
	DigitSet getDigitsForWrite() {
		return timeDigits;
	}
	DigitSet getDigitsForRead() {
		return timeDigits;
	}
	void latch() {}
	void unlatch() {}
}
class TimeWithLatch extends Time {
	DigitSet latchDigits = timeDigits;
	
	@Override
	DigitSet getDigitsForRead() {
		return latchDigits;
	}
	@Override
	void latch() {
		latchDigits = latchDigits.copy();	// this way, if it's already latched, we copy that same copy 
	}
	@Override
	void unlatch() {
		latchDigits = timeDigits;
	}
}