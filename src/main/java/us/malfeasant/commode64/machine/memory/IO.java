package us.malfeasant.commode64.machine.memory;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import us.malfeasant.commode64.machine.video.Video;

public class IO extends Chunk {
	public final ObjectProperty<Video> videoProp = new SimpleObjectProperty<>();
	// TODO properties for SID, CIAs, I/O expansions, anything else?
	
	/**
	 * Only constructor- not much to do here.
	 */
	public IO() {
		super(null, -1);
	}
	
	@Override
	byte peek(short addr) {
		byte data = 0;
		switch (addr & 0xc00) {
		case 0x000:	// VIC
			data = (byte) videoProp.get().peek(addr & 0x3ff);	// TODO make video return byte? but that's a lot of work...
			break;
		case 0x400:	// SID
			// TODO
			break;
		case 0x800:	// coloram
			data = (byte) videoProp.get().peek(addr & 0x3ff);
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
	void poke(short addr, byte data) {
		switch (addr & 0xc00) {
		case 0x000:	// VIC
			videoProp.get().poke(addr, data);
			break;
		case 0x400:	// SID
			// TODO
			break;
		case 0x800:	// coloram
			videoProp.get().poke(addr & 0x3ff, data & 0xf);
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
