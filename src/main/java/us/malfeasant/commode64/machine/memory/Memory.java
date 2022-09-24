package us.malfeasant.commode64.machine.memory;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Models the address/data buses and related signals- including the video chip's special circuit enabling 12-bit fetches,
 * and char rom in banks 0 and 2
 * @author Malfeasant
 */
public class Memory {
	private final Chunk[] ram;	// ram is an array of arrays
	private final Chunk charom;
	private final Chunk[] basic;
	private final Chunk[] kernal;
	private final IO io;	// this one is special
	private final Chunk scratch;	// used by ultimax mode for invalid addresses- reads garbage, writes are ignored
	
	private final Chunk[] cpureadmap;	// the view of ram from the cpu (64k) for reads (ram, rom, i/o)
	private final Chunk[] cpuwritemap;	// the view of ram from the cpu (64k) for writes (mostly ram)
	private final Chunk[] vicreadmap;	// the view of ram from the vic (16k)
	
	public final ObjectProperty<Chunk[]> cartLo;	// may or may not be present... 
	public final ObjectProperty<Chunk[]> cartHi;
	
	public final BooleanProperty aec;	// Allows CPU to drive the bus- when false, Vic drives it.
	public final BooleanProperty ba;	// Allows CPU to run- when pulled low, cpu pauses (can finish a write)
	public final BooleanProperty irq;	// Interrupt request- aggregate from all sources
	
	public final BooleanProperty va14;	// controlled by CIA, selects VIC's bank
	public final BooleanProperty va15;
	
	// On a real machine, these are active low signals- but I'm using true/false in the logical sense, so true means
	// it's enabled, which would be a 0 in a real machine.
	public final BooleanProperty exrom;	// cartridge input- true=cart rom at 8000-9fff, else ram (except ultimax)
	public final BooleanProperty game;		// cartridge input- true=cart rom at a000-bfff, else ram (except ultimax)
	private final BooleanProperty ultimax;	// specific combination of exrom & game, changes a lot of stuff
	
	public final BooleanProperty loram;	// controlled by cpu- false=basic rom at a000-bfff, else ram
	public final BooleanProperty hiram;	// controlled by cpu- false=kernal rom at e000-ffff, else ram
	public final BooleanProperty charen;	// controlled by cpu- false=i/o at d000-dfff, else char rom, unless...
	
	private byte portBits;	// cpu i/o port bits (stored here because otherwise could be lost if direction set to input)
	private byte portDirection;	// direction for above- true = output
	
	boolean cpumapvalid = false;	// false means we need to setup the memory map before next access
	boolean vicmapvalid = false;	// same as above but for vic
	
	public Memory() {
		ram = Chunk.ram();
		io = new IO();
		scratch = new Scratch();
		charom = Chunk.charrom();
		basic = Chunk.basic();
		kernal = Chunk.kernal();
		
		cartLo = new SimpleObjectProperty<>();
		cartHi = new SimpleObjectProperty<>();
		
		aec = new SimpleBooleanProperty(false);
		ba = new SimpleBooleanProperty(false);
		irq = new SimpleBooleanProperty(false);
		
		va14 = new SimpleBooleanProperty(false);	// driven by CIA, bit 14 of VIC memory address
		va15 = new SimpleBooleanProperty(false);	// driven by CIA, bit 15 of VIC memory address
		
		exrom = new SimpleBooleanProperty(false);
		game = new SimpleBooleanProperty(false);
		ultimax = new SimpleBooleanProperty(false);
		ultimax.bind(Bindings.and(game, Bindings.not(exrom)));	// so ultimax will be true if exrom=false and game=true
		
		loram = new SimpleBooleanProperty(false);
		hiram = new SimpleBooleanProperty(false);
		charen = new SimpleBooleanProperty(false);
		
		cpureadmap = new Chunk[0x10];
		cpuwritemap = new Chunk[0x10];
		vicreadmap = new Chunk[4];
		
		// Many signals to watch, but they don't change super often, so will just use invalidation listeners-
		// Anything touches them, they're invalidated, then all will be recomputed on the next memory access.
		loram.addListener(p -> cpumapvalid = false);
		hiram.addListener(p -> cpumapvalid = false);
		charen.addListener(p -> cpumapvalid = false);
		game.addListener(p -> cpumapvalid = false);
		exrom.addListener(p -> cpumapvalid = false);
		ultimax.addListener(p -> {	// if ultimax mode switches, that affects both cpu and vic view of ram
			cpumapvalid = false;
			vicmapvalid = false;
		});
		
		va14.addListener(p -> vicmapvalid = false);
		va15.addListener(p -> vicmapvalid = false);
	}
	
	private byte getHigh(short addr) {	// helper function to read top 4 bits of address
		return (byte) ((addr >> 12) & 0xf);
	}
	
	private void updateFlags() {	// called whenever i/o port or direction changes
		cpumapvalid = false;
		loram.set((portDirection & 1) != 0 ? (portBits & 1) == 0 : false);	// real circuit has pullup, so reads high if
		hiram.set((portDirection & 2) != 0 ? (portBits & 2) == 0 : false);	// switched to input- but, these are active
		charen.set((portDirection & 4) != 0 ? (portBits & 4) == 0 : false);	// low, so meaning is reversed- high = false
		// TODO cassette data out
		// TODO cassette switch
		// TODO cassette motor
	}
	
	/**
	 * Models a write from CPU- almost always goes to RAM, but may need to be intercepted to go to i/o-
	 * also, ultimax mode stops writes to everything beyond 0x1000 (except i/o)
	 * @param addr - Address to write to
	 * @param data - Data to be written
	 */
	public void poke(short addr, byte data) {
		if ((addr & 0xfffe) == 0) {	// i/o port or direction
			if (addr == 0) {
				portDirection = data;
			} else {
				portBits = data;
			}
			updateFlags();
		} else {
			if (!cpumapvalid) {
				cpumap();
			}
			var ha = getHigh(addr);
			addr &= 0xfff;	// mask out low bits
			cpuwritemap[ha].poke(addr, data);
		}
	}
	
	private byte readPort() {
		byte data = 0;
		if ((portDirection & 1) == 0) {	// input bit
			if (loram.get()) data |= 1;
		} else {
			data |= (portBits & 1);
		}
		if ((portDirection & 2) == 0) {	// input bit
			if (hiram.get()) data |= 2;
		} else {
			data |= (portBits & 2);
		}
		if ((portDirection & 4) == 0) {	// input bit
			if (charen.get()) data |= 4;
		} else {
			data |= (portBits & 4);
		}
		if ((portDirection & 8) == 0) {
			data |= 8;	// bit will read high if input
		} else {
			data |= (portBits & 8);	// read back what was last written
		}
		// TODO read cassette switch
		if ((portDirection & 0x20) == 0) {
			data |= 0x20;	// bit will read high if input
		} else {
			data |= (portBits & 0x20);	// read back what was last written
		}
		return data;
	}
	
	/**
	 * Models a read from CPU
	 * @param addr - Address to read from
	 * @return - Data read from appropriate source
	 */
	public byte peek(short addr) {
		if ((addr & 0xfffe) == 0) {	// i/o port or direction
			return (addr == 0) ? portDirection : readPort();
		}
		if (!cpumapvalid) {
			cpumap();
		}
		var ha = getHigh(addr);
		addr &= 0xfff;	// mask out low bits
		return cpureadmap[ha].peek(addr);
	}
	
	private void cpumap() {	// setup the cpu's view of memory
		if (ultimax.get()) {	// shortcut for lots of changes
			cpureadmap[0] = ram[0];
			cpuwritemap[0] = ram[0];
			for (int i = 1; i < 0x10; i++) {	// will overwrite roms & i/o after
				cpureadmap[i] = scratch;
				cpuwritemap[i] = scratch;
			}
			cpureadmap[0xd] = io;
			cpuwritemap[0xd] = io;
			cpureadmap[8] = cartLo.get()[0];
			cpureadmap[9] = cartLo.get()[1];
			cpureadmap[0xe] = cartHi.get()[0];
			cpureadmap[0xf] = cartHi.get()[1];
		} else {
			for (int i = 0; i < 0x10; i++) {
				cpuwritemap[i] = ram[i];
				cpureadmap[i] = ram[i];	// will overwrite roms & i/o after
			}
			if (!loram.get() && !hiram.get() && !game.get()) {	// basic rom
				cpureadmap[0xa] = basic[0];
				cpureadmap[0xb] = basic[1];
			}
			if (!hiram.get() && (!game.get() || (exrom.get() && game.get()))) {	// kernal rom
				cpureadmap[0xe] = kernal[0];
				cpureadmap[0xf] = kernal[1];
			}
			if (charen.get() && (	// char rom
					(!hiram.get() && !game.get()) || 
					(!loram.get() && !game.get()) || 
					(!hiram.get() && exrom.get() && game.get()))) {
				cpureadmap[0xd] = charom;
			}	// TODO still need i/o, roml, romh...
		}
		cpumapvalid = true;
	}
	
	private void vicmap() {	// setup the video chip's view of memory
		if (ultimax.get()) {	// vic sees 12k of ram and 2nd half of hi cart rom (but cpu can't write past 4k of ram)
			for (int i = 0; i < 3; i++) {
				vicreadmap[i] = ram[i];
			}
			vicreadmap[3] = cartHi.get()[1];
		} else {
			int ha = (va14.get() ? 4 : 0);
			ha |= va15.get() ? 8 : 0;
			for (int i = 0; i < 4; i++) {
				vicreadmap[i] = ram[i + ha];
			}
			if (!va14.get()) {	// special circuit to character generator rom
				vicreadmap[1] = charom;
			}
		}
		vicmapvalid = true;
	}
	
	/**
	 * Models a fetch from VIC
	 * @param addr - 14-bit address to read from- top 2 bits come from CIA #? TODO which?  and how?
	 * @return - 12-bits of data- top 4 bits come from color ram
	 */
	public short vread(short addr) {
		if (!vicmapvalid) vicmap();
		var ha = getHigh(addr);
		assert (ha < 4) : "Video read: Address " + addr + " out of range.";
		return 0;// (short) ((coloram[addr & 0x3ff] << 8) | vicreadmap[ha][addr & 0xfff]);
	}
}
