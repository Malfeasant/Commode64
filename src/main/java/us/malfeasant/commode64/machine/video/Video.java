package us.malfeasant.commode64.machine.video;

import java.util.EnumSet;
import java.util.Set;
import java.util.TreeSet;

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
	
	private int imageX;	// the x coordinate within the generated image- different from raster position
	private int imageY;	// the y coordinate within the generated image- different from raster position
	int refreshCounter;	// not sure if there is anything to be gained by simulating this, but it shouldn't hurt much...
	
	final short[] lineBuffer = new short[40];	// stores result of c-access for 8 lines- includes color nybble too
	
	private long outBuffer;	// stores character pixels- each new pixel added shifts to left
	// where these are read from depends on smooth scrolling register
	
	int rasterX;	// 9 bits, horizontal coordinate for light pen, sprites, border compare
	int rasterCompare;	// 9-bits used to set an interrupt to occur on a given line
	int rasterCurrent;	// 9-bits, the current raster line being scanned
	
	CycleType currentCycle;
	int vmbase = 0;	// 4 bits from d018, determines where video matrix (and sprite pointers) come from
	int chbase = 0;	// 3 bits from d018, determines where character pattern/bitmap fetches come from
	
	int address;	// temporarily holds address before placing on bus
	
	boolean bad;	// ongoing bad line condition- will be stealing cycles for c fetches
	int preBA = 4;	// counts cycles between asserting ba and aec- if preBA=0, ok to steal a cycle
	
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
	final Set<Sprite> spritesThisLine;	// holds the sprites that have matched y position
	final Set<Sprite> spritesDisplaying;	// holds sprites that have matched x position
	
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
		spritesThisLine = new TreeSet<>((o1, o2) -> { return o1.which - o2.which; });
		spritesDisplaying = new TreeSet<>((o1, o2) -> { return o1.which - o2.which; });
		for (int i=0; i<8; ++i) {
			sprites[i] = new Sprite(i);
		}
		
		currentCycle = CycleType.values()[0];
	}
	/**
	 * Advances one cpu clock cycle- so 8 pixel clock cycles.  Handles memory accesses. 
	 */
	public void crystalTick() {
		if (baWrapper.get()) {	// if nothing is pulling ba low, reset counter
			preBA = 4;
		} else {
			if (--preBA < 0) {	// start counting down but
				preBA = 0;	// don't pass 0
			}
		}
		aecWrapper.set(false);	// always low in first half of cycle
		currentCycle.clockLo(this);	// modifies internal state
		emitPixels();
		aecWrapper.set(preBA != 0);	// aec normally goes high in second half, unless ba has been low for 3 cycles
		currentCycle.clockHi(this);	// Always call second cycle- it must check aec to determine if it can steal cycle
		emitPixels();	// has to be done twice, after each cycle half
		currentCycle = currentCycle.nextFor(variantProperty.get());	// advance the cycle
	}
	
	private void emitPixels() {	// emits block of 4 pixels
		for (var count = 0; count < 4; count++) {
			var spritePix = -1;	// transparent
			var x = rasterX + count;
			for (var sprite : spritesThisLine) {
				if (sprite.x == x) {
					spritesDisplaying.add(sprite);
				}
				if (sprite.sequencer == 0) {	// all bits have been shifted
					spritesDisplaying.remove(sprite);
				}
			}
			for (var sprite : spritesDisplaying) {
				if (sprite.multiMid < 0) {
					
				} else {
					spritePix = sprite.multiMid;
				}
			}
		}
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
		baWrapper.set(false);
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
