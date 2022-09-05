package us.malfeasant.commode64.machine.video;

import javafx.geometry.Rectangle2D;

/**
 * Represents the different chip revisions- main differences: 
 * Dimensions:
 * NTSC_OLD- likely they wanted a nice power of 2 count of cycles for line length (64), but this was a little
 * shorter than a standard line, so caused sync problems with some tvs.  Also frame rate was 61Hz rather than 60.
 * NTSC_NEW- went to 63 cycles per line, which was an exact match to the NTSC line length.  framerate was 59.8 HZ.
 * PAL dimensions didn't change.
 * Palette: Early chips had 5 luma levels, so many colors were the same brightness, making it difficult to work on a
 * b&w tv.  Later chips expanded this to 9, so no more than 2 colors shared a brightness.
 * 
 * This class houses all of the magic numbers- some that mark special cycles, i.e. end of line, end of frame,
 * when to start sprite fetches, and other stuff like viewport dimensions... more to come probably...
 * @author Malfeasant
 */
public enum Variant {
	NTSC_OLD(63, 233,
			76, 41, 411, 234),
	NTSC_NEW(64, 234,
			77, 41, 418, 235),
	PAL_OLD(62, 283,
			76, 16, 403, 284),
	PAL_NEW(62, 283,
			76, 16, 403, 284);
	final int endOfLine;
	final int endOfFrame;
	// The following are fixed relative to either the beginning (+) or end (-) of line
	final int gFetchStart = 15;	// c fetch starts 1 cycle before!
	final int gFetchEnd = 55;
	final int sFetchStart = -6;	// then every 2 after
	final int refreshStart = 10;
	final Rectangle2D viewport;
	Variant(int eol, int eof,
			int vpx, int vpy, int vpw, int vph) {
		endOfLine = eol;
		endOfFrame = eof;
		viewport = new Rectangle2D(vpx, vpy, vpw, vph);
	}
}
