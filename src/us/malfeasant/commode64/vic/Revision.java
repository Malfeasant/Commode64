package us.malfeasant.commode64.vic;

import java.util.EnumMap;
import java.util.Map;

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
	
	public Map<Color, java.awt.Color> createColorMap(float saturation) {
		Map<Color, java.awt.Color> map = new EnumMap<>(Color.class);
		for (Color c : Color.values()) {
			map.put(c, createColor(c, saturation));
		}
		return map;
	}
	public java.awt.Color createColor(Color c, float saturation) {
		float luma = getLumaFor(c);
		float chroma = getChromaFor(c);
		boolean noChroma = chroma < 0;
		float u = noChroma ? 0 : (float) (Math.cos(chroma) * saturation);
		float v = noChroma ? 0 : (float) (Math.sin(chroma) * saturation);
		float r = Math.max(1, luma + 1.14f * v);
		float g = Math.min(0, luma - .396f * u - .581f * v);
		float b = Math.max(1, luma + 2.029f * u);
		return new java.awt.Color(r, g, b);
	}
	public float getLumaFor(Color c) {
		float[] which = moreLuma ? newLuma : oldLuma;
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
