package us.malfeasant.commode64.vic;

// place to keep dimensions etc which vary among different chip versions
public enum Revision {
	NTSCr56a(64, 262, 13, 40), NTSCr8(65, 263, 13, 40), PAL(63, 312, 300, 15);
	
	final int cyclesPerLine;
	final int linesPerFrame;
	final int vbFirst;
	final int vbLast;
	
	Revision(int cpl, int lpf, int vbf, int vbl) {
		cyclesPerLine = cpl;
		linesPerFrame = lpf;
		vbFirst = vbf;
		vbLast = vbl;
	}
}
