package us.malfeasant.commode64.machine;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import us.malfeasant.commode64.machine.video.Variant;
import us.malfeasant.commode64.machine.video.Video;
import us.malfeasant.commode64.timing.CrystalListener;
import us.malfeasant.commode64.timing.PowerListener;

/**
 * Contains all the bits that mimic real hardware...
 * @author Malfeasant
 */
public class Machine implements CrystalListener, PowerListener {
	private Video video;
	private final ObjectProperty<Variant> variantProperty;
	public Machine() {
		video = new Video();
		variantProperty = new SimpleObjectProperty<>();
	}
	public void powerTick() {
		// TODO
	}
	
	public void crystalTick(int howmany) {
		// TODO
	}
	
	public ReadOnlyObjectProperty<Image> imageProperty() {
		return video.imageProperty();
	}
	public ReadOnlyObjectProperty<Rectangle2D> viewportProperty() {
		return video.viewportProperty();
	}
	public ObjectProperty<Variant> variantProperty() {
		return variantProperty;
	}
}
