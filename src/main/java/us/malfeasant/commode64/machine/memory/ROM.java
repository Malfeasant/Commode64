package us.malfeasant.commode64.machine.memory;

public class ROM extends Chunk {
	protected ROM(byte[] contents, int offset) {
		super(contents, offset);
		if (contents.length < 0x1000) throw new IllegalArgumentException("ROM must be at least 4k.");
	}

	@Override
	void poke(short addr, byte data) {
		// It's up to top level memory object to direct writes to underlying RAM when appropriate
		throw new IllegalStateException("Attempt to write to ROM.");
	}
}
