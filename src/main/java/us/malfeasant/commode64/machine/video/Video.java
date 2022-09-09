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
	private boolean activeX;	// if generated pixels are valid in the x dimension
	private boolean activeY;	// if generated pixels are valid in the y dimension
	
	private final short[] lineBuffer = new short[40];	// stores result of c-access for 8 lines- includes color nybble too
	
	private long outBuffer;	// stores character pixels- each new pixel added shifts to left
	// where these are read from depends on smooth scrolling register
	
	CycleType currentCycle;
	int vmbase = 0;	// 4 bits from d018, determines where video matrix (and sprite pointers) come from
	int chbase = 0;	// 3 bits from d018, determines where character fetches come from
	
	boolean bad;	// ongoing bad line condition- will be stealing cycles for c fetches
	boolean aec;	// if true, cpu can run bus cycle.  if false, vic will be stealing a cycle.
	int preBA = 4;	// counts cycles between asserting ba and aec- if ba=0, ok to steal a cycle
	
	int spriteCounter = 0;	// where to fetch sprite data from (loaded with p cycle, used for each s cycle
	final Sprite[] sprites;
	
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
			viewportWrapper.set(now.viewport);
		});
		
		sprites = new Sprite[8];
		for (int i=0; i<8; ++i) {
			sprites[i] = new Sprite();
		}
		currentCycle = CycleType.values()[0];
	}
	/**
	 * Advances one cpu clock cycle- so 8 pixel clock cycles.  Does c-access, if needed/allowed does g-access as well.
	 * Also s-access for sprites. 
	 */
	public void crystalTick() {
		currentCycle.advance(this);	// modifies internal state, including replacing currentCycle with the next
		
		// TODO more
	}
	
	public void poke(int addr, int data) {
		addr &= 0x3f;	// any other bits are ignored
		switch (addr) {
		case 0x15:	// sprite enable reg
			for (Sprite s : sprites) {
				s.enabled = (data & 1) != 0;	// check rightmost bit
				data >>= 1;	// shift right to be ready for the next
			}
			break;
		case 0x18:	// 53272: memory control reg
			vmbase = (data & 0xf0 << 6);	// bits 7-4 move to bits 13-10
			chbase = (data & 0xe << 10);	// bits 3-1 move to bits 13-11
			break;
		}
	}
	
	public int peek(int addr) {
		int data = 0;
		addr &= 0x3f;	// any other bits are ignored
		switch (addr) {
		case 0x15:	// sprite enable reg
			for (Sprite s : sprites) {
				data <<= 1;	// shift left (if first, will be 0 anyway)
				if (s.enabled) data |= 1;
			}
			break;
		case 0x18:	// 53272: memory control reg
			data |= (vmbase >> 6);
			data |= (chbase >> 10);
			break;
		}
		
		return data;
	}
}
