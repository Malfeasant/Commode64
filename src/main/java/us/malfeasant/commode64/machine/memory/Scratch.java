package us.malfeasant.commode64.machine.memory;

/**
 * Models a block of memory not connected to anything- reads return junk, writes are ignored
 * @author Malfeasant
 */
class Scratch extends Chunk {
	Scratch() {
		super(null, -1);	// no backing array
	}
	
	@Override
	int peek(int addr) {
		return -1;
	}
	@Override
	void poke(int addr, int data) {
		// nothing!
	}
}
