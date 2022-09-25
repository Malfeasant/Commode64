package us.malfeasant.commode64.machine.video;

/**
 * Simple enum representing who needs to steal a CPU cycle- needed because sprites overlap each other, so if a given 
 * sprite's fetch is over and it can release the bus, it doesn't pull the rug out from under the next sprite that has
 * already signaled it needs it next.
 * 
 * @author Malfeasant
 */
enum StunSource {
	SPRITE0, SPRITE1, SPRITE2, SPRITE3,
	SPRITE4, SPRITE5, SPRITE6, SPRITE7,
	CHARPTR;
}
