package us.malfeasant.commode64.timing;

public enum Power implements Fraction {
	US(60), EU(50);
	private final int cycles;
	Power(int c) {
		cycles = c;
	}
	@Override
	public int numerator() {
		return cycles;
	}
	@Override
	public int denominator() {
		return 1;
	}
}