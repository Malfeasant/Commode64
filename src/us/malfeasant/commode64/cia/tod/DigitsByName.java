package us.malfeasant.commode64.cia.tod;

enum DigitsByName {
	T(0xf, 9), SL(0xf, 9), SH(7, 5), ML(0xf, 9), MH(7, 5), HL(0xf, 9, 1), HH(9, 2);
	final int mask;
	final int comp;
	final int init;
	DigitsByName(int m, int c) {
		this(m, c, 0);
	}
	DigitsByName(int m, int c, int i) {
		mask = m;
		comp = c;
		init = i;
	}
}