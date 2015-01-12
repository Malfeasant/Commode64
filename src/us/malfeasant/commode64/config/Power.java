package us.malfeasant.commode64.config;

public enum Power {
	// These are somewhat magic numbers, and are dependent on Invariants.INTERVAL_MS
	US(3, 10), EU(1, 4);
	
	private final int numer;
	private final int denom;
	
	Power(int n, int d) {
		numer = n;
		denom = d;
	}
	
	// TODO add methods to schedule and receive ticks
}
