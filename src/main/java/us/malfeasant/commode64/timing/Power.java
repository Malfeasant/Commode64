package us.malfeasant.commode64.timing;

/**
 * Represents ac power, how many cycles per second, as North American (60Hz) or European (50Hz)
 * 
 * @author Malfeasant
 */
public enum Power {
	NA(60), EU(50);
	public final int cycles;
	public final int seconds = 1;
	Power(int c) {
		cycles = c;
	}
}