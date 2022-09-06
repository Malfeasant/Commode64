package us.malfeasant.commode64.machine.video;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;
import us.malfeasant.commode64.machine.Memory;

public class Video {
	private final ReadOnlyObjectWrapper<WritableImage> imageWrapper;
	private final ReadOnlyObjectWrapper<Rectangle2D> viewportWrapper;
	private final ReadOnlyBooleanWrapper baWrapper;
	private final ReadOnlyBooleanWrapper aecWrapper;
	
	public final ReadOnlyObjectProperty<WritableImage> imageProperty;
	public final ReadOnlyObjectProperty<Rectangle2D> viewportProperty;
	public final ReadOnlyBooleanProperty baProperty;	// signals to cpu to pause execution
	public final ReadOnlyBooleanProperty aecProperty;	// signals to cpu to release bus control
	public final ObjectProperty<Memory> memoryProperty;	// points to memory handler
	public final ObjectProperty<Variant> variantProperty;	// chip revision bits
	
	private int rasterByte;	// cycle within a line- all memory accesses are determined by this
	private int rasterLine;	// the internal line count, separate from the y coordinate of the generated image
	private int rasterX;	// the x coordinate- this does not get reset at the same time as rasterByte.
	private int rasterY;	// the actual y coordinate within the generated image
	private int refreshCounter;	// not sure if there is anything to be gained by simulating this, but it shouldn't hurt much...
	private int preBA;	// counts cycles between asserting ba and aec
	private boolean activeX;	// if generated pixels are valid in the x dimension
	private boolean activeY;	// if generated pixels are valid in the y dimension
	
	private final short[] lineBuffer;	// stores result of c-access for 8 lines- includes color nybble too
	
	private long outBuffer;	// stores character pixels- each new pixel added shifts to left
	// where these are read from depends on smooth scrolling register
	
	public Video() {
		imageWrapper = new ReadOnlyObjectWrapper<>();	// image that we render into
		imageProperty = imageWrapper.getReadOnlyProperty();
		viewportWrapper = new ReadOnlyObjectWrapper<>();	// crops the image
		viewportProperty = viewportWrapper.getReadOnlyProperty();
		baWrapper = new ReadOnlyBooleanWrapper();
		baProperty = baWrapper.getReadOnlyProperty();
		aecWrapper = new ReadOnlyBooleanWrapper();
		aecProperty = aecWrapper.getReadOnlyProperty();
		memoryProperty = new SimpleObjectProperty<>();
		variantProperty = new SimpleObjectProperty<>();
		
		variantProperty.addListener((b, then, now) -> {	// if variant is changed, need to change image and viewport
			imageWrapper.set(new WritableImage(now.endOfLine * 8, now.endOfFrame));
			viewportWrapper.set(now.viewport);	// TODO get from variant
		});
		
		lineBuffer = new short[40];
	}
	/**
	 * Advances one cpu clock cycle- so 8 pixel clock cycles.  Does c-access, if needed/allowed does g-access as well.
	 * Also s-access for sprites. 
	 */
	public void crystalTick() {
		var variant = variantProperty.get();
		if (++rasterByte > variant.endOfLine) {
			rasterByte = 0;
			if (++rasterLine > variant.endOfFrame) {
				rasterLine = 0;
			}
		}
		
		// TODO more
	}
}
