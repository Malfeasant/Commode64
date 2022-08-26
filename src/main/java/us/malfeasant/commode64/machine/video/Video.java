package us.malfeasant.commode64.machine.video;

import java.nio.ByteBuffer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public class Video {
	private final WritableImage image;
	private final ReadOnlyObjectWrapper<Image> imageProperty;
	private final ReadOnlyObjectWrapper<Rectangle2D> viewportProperty;
	private final ObjectProperty<ByteBuffer> memoryProperty;	// points to 16k block of RAM
	private final ObjectProperty<Variant> variantProperty;	// chip revision bits
	
	int rasterByte;
	int rasterLine;
	
	public Video() {
		image = new WritableImage(520, 262);
		imageProperty = new ReadOnlyObjectWrapper<>(image);	// image that we render into
		viewportProperty = new ReadOnlyObjectWrapper<>();	// crops the image
		memoryProperty = new SimpleObjectProperty<>();
		variantProperty = new SimpleObjectProperty<>();
	}
	public ReadOnlyObjectProperty<Image> imageProperty() {
		return imageProperty.getReadOnlyProperty();
	}
	public ReadOnlyObjectProperty<Rectangle2D> viewportProperty() {
		return viewportProperty.getReadOnlyProperty();
	}
	public ObjectProperty<ByteBuffer> memoryProperty() {
		return memoryProperty;
	}
	public ObjectProperty<Variant> variantProperty() {
		return variantProperty;
	}
	/**
	 * Advances one cpu clock cycle- so 8 pixel clock cycles.  Does c-access, if needed/allowed does g-access as well. 
	 * @return 8x 4-bit pixels packed as an int
	 */
	public int getPixelBlock() {
		variantProperty.get().advance(this);
		return 0;	// TODO
	}
}
