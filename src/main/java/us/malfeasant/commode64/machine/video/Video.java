package us.malfeasant.commode64.machine.video;

import java.util.EnumSet;
import java.util.Set;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;
import us.malfeasant.commode64.machine.memory.Memory;

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
	
	private int rasterX;	// the x coordinate- this does not get reset at the same time as rasterByte.
	private int rasterY;	// the actual y coordinate within the generated image
	int refreshCounter;	// not sure if there is anything to be gained by simulating this, but it shouldn't hurt much...
	private boolean activeX;	// if generated pixels are valid in the x dimension
	private boolean activeY;	// if generated pixels are valid in the y dimension
	
	final short[] lineBuffer = new short[40];	// stores result of c-access for 8 lines- includes color nybble too
	
	private long outBuffer;	// stores character pixels- each new pixel added shifts to left
	// where these are read from depends on smooth scrolling register
	
	int rasterCompare;	// 9-bits used to set an interrupt to occur on a given line
	int rasterCurrent;	// 9-bits, the current raster line being scanned
	
	CycleType currentCycle;
	int vmbase = 0;	// 4 bits from d018, determines where video matrix (and sprite pointers) come from
	int chbase = 0;	// 3 bits from d018, determines where character pattern/bitmap fetches come from
	
	int address;	// temporarily holds address before placing on bus
	
	boolean bad;	// ongoing bad line condition- will be stealing cycles for c fetches
	boolean aec;	// if true, cpu can run bus cycle.  if false, vic will be stealing a cycle.
	int preBA = 4;	// counts cycles between asserting ba and aec- if ba=0, ok to steal a cycle

	boolean csel = false;	// border adjust for fine scrolling- true = 40 columns, false = 38 columns
	int xscroll = 0;	// 3 bits, adjusts x position of video matrix for fine scrolling
	boolean rsel = false;	// border adjust for fine scrolling- true = 25 rows, false = 24 rows
	int yscroll = 0;	// 3 bits, adjusts y position of video matrix for fine scrolling
	
	boolean res = false;	// reset bit- older chips used this to disable the chip, but newer ones ignore it.
	boolean den = false;	// display enable - false stops bad lines & border decodes
	boolean ecm = false;	// extended color mode
	boolean bmm = false;	// bitmap mode
	boolean mcm = false;	// multicolor mode
	
	int borderColor;	// d020
	int backColor0;	// d021 normal text mode background color
	int backColor1;	// d022 bg color 1, used in mcm and ecm
	int backColor2;	// d023 bg color 2, used in mcm and ecm
	int backColor3;	// d024 bg color 3, used in ecm
	
	int spmc0;	// Sprite multicolor 0 (shared by all sprites)
	int spmc1;	// Sprite multicolor 1 (shared by all sprites)
	
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
		currentCycle.clockLo(this);	// modifies internal state, including replacing currentCycle with the next
		// TODO check if ok to steal cycle, do clockHi()
		// TODO more
	}
	
	private Register selectRegister(int addr) {
		addr &= 0x3f;	// any other bits are ignored
		return (addr < 0x30) ? Register.values()[addr] : Register.D02F;
	}
	public void poke(int addr, int data) {
		selectRegister(addr).poke(this, data);
	}
	
	public int peek(int addr) {
		return selectRegister(addr).peek(this);
	}
	
	private final Set<StunSource> stunSources = EnumSet.noneOf(StunSource.class);
	/**
	 * Negate BA signal, getting ready to steal cycles from CPU for extra fetches
	 * @param source which of sprites or character fetch needs extra cycles
	 */
	void prepareStun(StunSource source) {
		stunSources.add(source);
		baWrapper.set(false);	// TODO something needs to watch this and count cycles, then 3 cycles after this first
		// goes false, also set aec to false to actually allow vic to steal cycles
	}
	/**
	 * Release bus to CPU- 
	 * @param source
	 */
	void releaseStun(StunSource source) {
		stunSources.remove(source);
		if (stunSources.isEmpty()) baWrapper.set(true);	// if nothing else needs it, gives the bus back to the CPU
	}
}
