package us.malfeasant.commode64.vic;

import java.awt.Color;

// place to keep dimensions etc which vary among different chip versions
public enum Revision {
	NTSCr56a(64, 262, 13, 40, 412, false), NTSCr8(65, 263, 13, 40, 412, true), PAL(63, 312, 300, 15, 404, true);
	
	final int cyclesPerLine;
	final int linesPerFrame;
	final int vbFirst;
	final int vbLast;
	final int firstX;
	private boolean moreLuma;
	
	Revision(int cpl, int lpf, int vbf, int vbl, int fx, boolean more) {
		cyclesPerLine = cpl;
		linesPerFrame = lpf;
		vbFirst = vbf;
		vbLast = vbl;
		firstX = fx;
		moreLuma = more;
	}
	
	public Color[] createColorMap(float saturation) {
		Color[] cmap = new Color[16];
		for (int c = 0; c < cmap.length; c++) {
			cmap[c] = createColor(c, saturation);
		}
		return cmap;
	}
	public Color createColor(int c, float saturation) {
		float luma = (moreLuma ? newLuma : oldLuma)[c];
		boolean noChroma = chroma[c] < 0;
		float u = noChroma ? 0 : (float) (Math.cos(chroma[c]) * saturation);
		float v = noChroma ? 0 : (float) (Math.sin(chroma[c]) * saturation);
		float r = Math.max(1, luma + 1.14f * v);
		float g = Math.min(0, luma - .396f * u - .581f * v);
		float b = Math.max(1, luma + 2.029f * u);
		return new Color(r, g, b);
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
