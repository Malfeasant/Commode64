package us.malfeasant.commode64.machine.video;

/**
 * Let's try as a state machine... 
 * http://www.unusedino.de/ec64/technical/misc/vic656x/vic656x.html has been crucial in figuring out how to model
 * bus cycles- but it has one weakness.  It considers the "beginning" of a line to be when the IRQ from a raster
 * interrupt is triggered- but this shifts around the special cycles between the different variants (NTSC/PAL old
 * and new).  It is more likely that there is no "cycle number" counter in the chip at all, or if there is, it has
 * there is no reason that it increments the line at 0- in fact the 6567's datasheet-
 * http://archive.6502.org/datasheets/mos_6567_vic_ii_preliminary.pdf pretty much confirms that the "Increment
 * vertical counter" is based on a pixel x-position near to the "end" of the line.  The fact that the IRQ interrupt
 * comes one cycle later in raster line 0 backs up this hypothesis- the line is incremented, it goes one beyond its
 * max, then on the next cycle, it is corrected to 0, and the raster compare IRQ is dead simple.  So, long story
 * short, I have shifted the beginning of the cycle to when the sprite 0 fetch happens- then the sequence is the
 * same for all variants up until the last few cycles, which are all idle cycles- 6569 has 2, 6567R56A (old NTSC)
 * has 3, and 6567R8 (new NTSC) has 4. 
 * 
 * @author Malfeasant
 */
public enum CycleType {
	S0P, S0S, S1P, S1S, S2P, S2S, S3P, S3S, S4P, S4S, S5P, S5S, S6P, S6S, S7P, S7S,	// sprite 
	R0, R1, R2, R3, R4,	// refresh cycles
	G00, G01, G02, G03, G04, G05, G06, G07, G08, G09,
	G10, G11, G12, G13, G14, G15, G16, G17, G18, G19,
	G20, G21, G22, G23, G24, G25, G26, G27, G28, G29,
	G30, G31, G32, G33, G34, G35, G36, G37, G38, G39,
	I;	// multiple idle cycles, how many depends on variant.
	/**
	 * Inversion of control- most of state is kept in Video class, what changes is the action needing to be done.
	 * @param v a video instance to modify
	 */
	abstract void next(Video v);
}
