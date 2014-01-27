package us.malfeasant.commode64.vic;

// place to keep dimensions etc which vary among different chip versions
public enum Revision {
	NTSCr56a(64, 262, 13, 40, true), NTSCr8(65, 263, 13, 40, false), PAL(63, 312, 300, 15, false);
	
	final int cyclesPerLine;
	final int linesPerFrame;
	final int vbFirst;
	final int vbLast;
	private boolean old;
	
	Revision(int cpl, int lpf, int vbf, int vbl, boolean ol) {
		cyclesPerLine = cpl;
		linesPerFrame = lpf;
		vbFirst = vbf;
		vbLast = vbl;
		old = ol;
	}
	
	public float getLumaFor(Color c) {
		float[] which = old ? oldLuma : newLuma;
		return which[c.ordinal()];
	}
	public float getChromaFor(Color c) {
		return chroma[c.ordinal()];
	}
	private static final float[] oldLuma = {
		 0,  1,   .25f,   .75f,   .5f,   .5f,  .25f,  .75f,  .25f,    .5f,    .5f, .25f, .5f,  .75f, .5f, .75f
	};
	private static final float[] newLuma = {
		 0,  1,   .25f,   .75f, .375f, .625f, .125f, .875f, .375f,  .125f,  .625f, .25f, .5f, .875f, .5f, .75f
	};
	private static final float[] chroma = {
		-1, -1, .3125f, .8125f, .125f, .625f,     0,   .5f, .375f, .4375f, .3125f,   -1,  -1, .625f,   0,   -1
	};
}
