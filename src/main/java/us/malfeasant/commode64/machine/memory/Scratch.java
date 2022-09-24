package us.malfeasant.commode64.machine.memory;

/**
 * Models a block of memory not connected to anything- reads return junk, writes are ignored
 * @author Malfeasant
 */
public class Scratch extends Chunk {
	public Scratch() {
		super(null, -1);	// no backing array
	}
	
	@Override
	byte peek(short addr) {
		return -1;
	}
	@Override
	void poke(short addr, byte data) {
		// nothing!
	}
}
