package us.malfeasant.commode64.machine.video;

/**
 * Holds all the sprite bits, so an array of these can be used instead of a bunch of things repeated 8 times...
 * @author Malfeasant
 */
class Sprite {
	private final int which;
	
	Sprite(int which) {
		this.which = which;
	}
	
	@Override
	public String toString() {
		return String.format("Sprite %d", which);
	}
	
	boolean enabled;	// d015
	boolean dma;		// fetch sprite data (stealing cycles from cpu as needed) and fill sequencer
	boolean display;	// shift bits out of sequencer after x coord matches
	boolean expandX;	// from register d01d
	boolean nowExpandX;	// above is sampled at particular time, then kept here
	boolean expandY;	// d017
	boolean notAgain;	// keeps track of y expand state between lines
	boolean firstLine;	// set when y position matches raster, resets several flags & counters
	boolean fgPriority;	// d01b false=sprite in front of fg
	boolean multicolor;	// d01c
	boolean collidedS;	// d01e this sprite has collided with another sprite
	boolean collidedV;	// d01f this sprite has collided with video matrix data
	
	int color;	// d027-2e
	int x;	// d000/2/4 etc + d010
	int y;	// d001/3/5 etc
	
	int pointer;	// 8 bits, left shifted 6 bits- where sprite data is located
	int mcbase;	// 6 bits- sprite data starting address counter- counts with each (non-repeated) line
	int mcount;	// 6 bits- address counter working copy- counts with each byte fetch of sprite
	int sequencer;	// shift register, holds the bit pattern of the sprite packed into 24 bits
}
