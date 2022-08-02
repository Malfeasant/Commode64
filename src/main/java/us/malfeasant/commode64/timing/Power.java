package us.malfeasant.commode64.timing;

/**
 * For an event every 1/10th second, how many cycles of ac power have there been.
 * 
 * @author Malfeasant
 */
public enum Power {
	US(6), EU(5);
	public final int cycles;
	public final int milliseconds = 100;
	Power(int c) {
		cycles = c;
	}
}