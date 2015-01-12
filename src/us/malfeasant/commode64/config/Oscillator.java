package us.malfeasant.commode64.config;

public enum Oscillator {
	// These are somewhat magic numbers, and are dependent on Invariants.INTERVAL_MS
	NTSC(56250, 11), PAL(709379, 144);
	
	private final int numer;
	private final int denom;
	
	Oscillator(int n, int d) {
		numer = n;
		denom = d;
	}
	// TODO add methods to schedule and receive ticks
}
