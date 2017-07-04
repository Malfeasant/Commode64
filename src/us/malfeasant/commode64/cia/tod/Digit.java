package us.malfeasant.commode64.cia.tod;

class Digit {
	private final int mask;
	private final int compare;
	private int bits;
	static final Digit ZERO = new Digit();
	
	Digit(DigitsByName name) {
		this.mask = name.mask;
		this.compare = name.comp;
		this.bits = name.init;
	}
	private Digit() {
		this.mask = 0;
		this.compare = 0;
		this.bits = 0;
	}
	
	void set(int in) {
		bits = in & mask;
	}
	int get() {
		return bits & mask;
	}
	boolean inc() {
		boolean match = bits == compare;
		bits = match ? 0 : 1 + bits;
		return match;
	}
}
