package us.malfeasant.commode64.machine.video;

/**
 * Holds all the sprite bits, so an array of these can be used instead of a bunch of things repeated 8 times...
 * @author Malfeasant
 */
class Sprite {
	boolean enabled;	// d015
	boolean expandX;	// from register d01d
	boolean nowExpandX;	// above is sampled at particular time, then kept here
	boolean expandY;	// d017
	boolean nowExpandY;	// above is sampled at particular time, then kept here
	boolean fgPriority;	// d01b false=sprite in front of fg
	boolean multicolor;
	boolean collidedS;	// this sprite has collided with another sprite
	boolean collidedV;	// this sprite has collided with video matrix data
	
	int color;	// d027-2e
	int x;	// d000/2/4 etc + d010
	int y;	// d001/3/5 etc
	
	int mcbase;	// sprite data starting address counter- counts with each (non-repeated) line
	int mc;	// address counter working copy- counts with each byte fetch of sprite
	int sequencer;	// shift register, holds the bit pattern of the sprite packed into 24 bits
}
