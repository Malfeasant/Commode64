package us.malfeasant.commode64.machine.video;

/**
 * Represents the different chip revisions- main differences: 
 * Dimensions:
 * NTSC_OLD- likely they wanted a nice power of 2 count of cycles for line length (64), but this was a little
 * shorter than a standard line, so caused sync problems with some tvs.  Also frame rate was 61Hz rather than 60.
 * NTSC_NEW- went to 63 cycles per line, which was an exact match to the NTSC line length.  framerate was 59.8 HZ.
 * PAL dimensions didn't change.
 * Palette: Early chips had 5 luma levels, so many colors were the same brightness, making it difficult to work on a
 * b&w tv.  Later chips expanded this to 9, so no more than 2 colors shared a brightness.
 * Houses all of the magic numbers that mark special cycles, i.e. end of line, end of frame, various others.
 * @author Malfeasant
 */
public enum Variant {
	NTSC_OLD(63, 233), NTSC_NEW(64, 234), PAL_OLD(62, 283), PAL_NEW(62, 283);
	private final int endOfLine;
	private final int endOfFrame;
	Variant(int eol, int eof) {
		endOfLine = eol;
		endOfFrame = eof;
	}
	
	void advance(Video v) {
		v.rasterByte++;
		if (v.rasterByte > endOfLine) {
			v.rasterByte = 0;
		}
	}
}
