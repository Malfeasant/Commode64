package us.malfeasant.commode64.timing;

/**
 * Magic numbers- represents a number of cyles per a number of seconds, which end up being an exact
 * representation of otherwise ugly repeating decimals
 * @author Malfeasant
 */
public enum Crystal {
	NTSC(11250000, 11), PAL(17734475, 18);
	public final int cycles;
	public final int seconds;
	Crystal(int c, int s) {
		cycles = c;
		seconds = s;
	}
}