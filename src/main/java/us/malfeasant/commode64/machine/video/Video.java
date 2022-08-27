package us.malfeasant.commode64.machine.video;

import java.nio.ByteBuffer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;

public class Video {
	private final ReadOnlyObjectWrapper<WritableImage> imageProperty;
	private final ReadOnlyObjectWrapper<Rectangle2D> viewportProperty;
	private final ObjectProperty<ByteBuffer> memoryProperty;	// points to 16k block of RAM
	private final ObjectProperty<Variant> variantProperty;	// chip revision bits
	
	int rasterByte;
	int rasterLine;
	
	public Video() {
		imageProperty = new ReadOnlyObjectWrapper<>();	// image that we render into
		viewportProperty = new ReadOnlyObjectWrapper<>();	// crops the image
		memoryProperty = new SimpleObjectProperty<>();
		variantProperty = new SimpleObjectProperty<>();
		
		variantProperty.addListener((b, then, now) -> {	// if variant is changed, need to change image and viewport
			imageProperty.set(new WritableImage(now.endOfLine * 8, now.endOfFrame));
			viewportProperty.set(now.viewport);	// TODO get from variant
		});
	}
	public ReadOnlyObjectProperty<WritableImage> imageProperty() {
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
	 */
	public void crystalTick() {
		variantProperty.get().advance(this);
		// TODO more
	}
}
