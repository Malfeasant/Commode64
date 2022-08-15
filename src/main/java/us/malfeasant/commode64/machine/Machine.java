package us.malfeasant.commode64.machine;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import us.malfeasant.commode64.timing.CrystalListener;
import us.malfeasant.commode64.timing.PowerListener;

/**
 * Contains all the bits that mimic real hardware...
 * @author Malfeasant
 */
public class Machine implements CrystalListener, PowerListener {
	private final ObjectProperty<Image> imageProperty;
	private final ObjectProperty<Rectangle2D> viewportProperty;
	private final WritableImage image;
	public Machine() {
		image = new WritableImage(520, 262);
		imageProperty = new SimpleObjectProperty<>(image);	// image that we render into		
		viewportProperty = new SimpleObjectProperty<>();	// image that we render into		
	}
	public void powerTick() {
		// TODO
	}
	
	public void crystalTick(int howmany) {
		// TODO
	}
	
	public ObjectProperty<Image> imageProperty() {
		return imageProperty;
	}
	
	public ObjectProperty<Rectangle2D> viewportProperty() {
		return viewportProperty;
	}
}
