package us.malfeasant.commode64.machine;

import java.util.Random;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
	private final WritableImage image;
	public Machine() {
		image = new WritableImage(520, 262);
		imageProperty = new SimpleObjectProperty<>(image);	// image that we render into		
	}
	private final Random rand = new Random();
	public void powerTick() {
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				image.getPixelWriter().setArgb(x, y, rand.nextInt());
			}
		}
	}
	
	public void crystalTick(int howmany) {
		// TODO
	}
	
	public void bindImageProperty(ObjectProperty<Image> other) {
		other.bind(imageProperty);
	}
}
