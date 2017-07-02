package us.malfeasant.commode64.config;

public enum Oscillator {
	/* These are somewhat magic numbers representing a number of cycles per number of seconds
	 * Done this way since otherwise they are ugly repeating decimals- this allows them to be stored as ints.
	 */
	NTSC(11250000, 11), PAL(17734475, 18);
	
	private final int cycles;
	private final int seconds;
	
	Oscillator(int c, int s) {
		cycles = c;
		seconds = s;
	}
	// TODO add methods to schedule and receive ticks
}
