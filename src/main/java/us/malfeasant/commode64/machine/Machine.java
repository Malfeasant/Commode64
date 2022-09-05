package us.malfeasant.commode64.machine;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;
import us.malfeasant.commode64.machine.video.Variant;
import us.malfeasant.commode64.machine.video.Video;
import us.malfeasant.commode64.timing.CrystalListener;
import us.malfeasant.commode64.timing.PowerListener;

/**
 * Contains all the bits that mimic real hardware...
 * @author Malfeasant
 */
public class Machine implements CrystalListener, PowerListener {
	private final Video video;
	private final Memory memory;
	
	public Machine() {
		video = new Video();
		memory = new Memory();
		video.memoryProperty.set(memory);
	}
	public void powerTick() {
		// TODO
	}
	
	public void crystalTick(int howmany) {
		for (int c = 0; c < howmany; c++) {
			// TODO tick CIAs, SID
			video.crystalTick();
			// TODO check status of BA, tick cpu (or not)
		}
	}
	
	public ReadOnlyObjectProperty<WritableImage> imageProperty() {
		return video.imageProperty;
	}
	public ReadOnlyObjectProperty<Rectangle2D> viewportProperty() {
		return video.viewportProperty;
	}
	public ObjectProperty<Variant> variantProperty() {
		return video.variantProperty;
	}
}
