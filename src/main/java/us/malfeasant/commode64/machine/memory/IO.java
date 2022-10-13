package us.malfeasant.commode64.machine.memory;

import us.malfeasant.commode64.machine.ChipSet;

public class IO extends Chunk {
	private final ChipSet chips;
	
	/**
	 * Only constructor- will use inherited array for color RAM
	 */
	IO(ChipSet cs) {
		super(new byte[0x400], 0);
		chips = cs;
	}
	
	@Override
	int peek(int addr) {
		byte data = 0;
		switch (addr & 0xc00) {
		case 0x000:	// VIC
			data = (byte) chips.videoProp.get().peek(addr & 0x3ff);
			break;
		case 0x400:	// SID
			// TODO
			break;
		case 0x800:	// coloram
			data = contents[addr & 0x3ff];	// TODO set high bits randomly?
			break;
		case 0xc00:	// CIAs, special I/O blocks
			switch (addr & 0xf00) {
			case 0xc00:	// CIA1
				// TODO
				break;
			case 0xd00:	// CIA2
				// TODO
				break;
			case 0xe00:	// I/O Expansion 1
				// TODO
				break;
			case 0xf00:	// I/O Expansion 2
				// TODO
				break;
			}
			break;
		}
		return data;
	}
	@Override
	void poke(int addr, int data) {
		switch (addr & 0xc00) {
		case 0x000:	// VIC
			chips.videoProp.get().poke(addr, data);
			break;
		case 0x400:	// SID
			// TODO
			break;
		case 0x800:	// coloram
			contents[addr & 0x3ff] = (byte) (data & 0xf);
			break;
		case 0xc00:	// CIAs, special I/O blocks
			switch (addr & 0xf00) {
			case 0xc00:	// CIA1
				// TODO
				break;
			case 0xd00:	// CIA2
				// TODO
				break;
			case 0xe00:	// I/O Expansion 1
				// TODO
				break;
			case 0xf00:	// I/O Expansion 2
				// TODO
				break;
			}
			break;
		}
	}
}
