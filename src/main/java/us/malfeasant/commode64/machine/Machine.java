package us.malfeasant.commode64.machine;

import org.tinylog.Logger;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;
import us.malfeasant.commode64.App;
import us.malfeasant.commode64.machine.memory.Memory;
import us.malfeasant.commode64.machine.video.Variant;
import us.malfeasant.commode64.machine.video.Video;
import us.malfeasant.commode64.timing.CrystalListener;
import us.malfeasant.commode64.timing.PowerListener;

/**
 * Contains all the bits that mimic real hardware...
 * @author Malfeasant
 */
public class Machine implements CrystalListener, PowerListener {
	private final Memory memory;
	private final ChipSet chips;
	
	public Machine() {
		
		var video = new Video();
		chips = new ChipSet(video);
		
		memory = new Memory(chips);
		video.memoryProperty.set(memory);
		
		// Should never fail, just using assert so this only gets called while running within eclipse...
		if (App.DEBUG) debug();
	}
	
	private void debug() {
		Logger.debug("Setting up test pattern...");
		for (var i = 0; i < 1000; i++) {
			memory.poke(0x400 + i, (byte) i);
			memory.poke(0xd800, 0xa);	// text color pink
		}
		memory.poke(0x7f8, 0x40);	// set sprite 0 pointer to pick up char rom
		memory.poke(0xd011, 0x10);	// sets DEN
		memory.poke(0xd015, 1);	// enable sprite 0
		//memory.poke(0xd016, 0);
		memory.poke(0xd018, 0x16);	// set vm to start at 0400, char pointers at 4352 (lowercase char set)
		memory.poke(0xd020, 0xa);	// set border to pink
		memory.poke(0xd021, 0x2);	// set bg to red
		memory.poke(0xd027, 0x8);	// sprite 0 to orange
	}
	
	public void powerTick() {
		// TODO
	}
	
	public void crystalTick(int howmany) {
		for (int c = 0; c < howmany; c++) {
			// TODO tick CIAs, SID
			chips.videoProp.get().crystalTick();
			// TODO check status of BA, tick cpu (or not)
		}
	}
	
	public ReadOnlyObjectProperty<WritableImage> imageProperty() {
		return chips.videoProp.get().imageProperty;
	}
	public ReadOnlyObjectProperty<Rectangle2D> viewportProperty() {
		return chips.videoProp.get().viewportProperty;
	}
	public ObjectProperty<Variant> variantProperty() {
		return chips.videoProp.get().variantProperty;
	}
}
