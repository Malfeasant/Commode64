package us.malfeasant.commode64.timing;

public enum Crystal implements Fraction {
	NTSC(45000000, 44), PAL(17734475, 18);
	private final int cycles;
	private final int seconds;
	Crystal(int c, int s) {
		cycles = c;
		seconds = s;
	}
	@Override
	public int numerator() {
		return cycles;
	}
	@Override
	public int denominator() {
		return seconds;
	}
}