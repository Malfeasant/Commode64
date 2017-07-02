package us.malfeasant.commode64.config;

public enum Power {
	// Number of cycles per second
	US(60), EU(50);
	
	private final int cycles;
	
	Power(int n) {
		cycles = n;
	}
	
	// TODO add methods to schedule and receive ticks
}
