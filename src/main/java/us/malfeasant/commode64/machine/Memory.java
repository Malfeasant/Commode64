package us.malfeasant.commode64.machine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

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
	private final byte[][] ram;	// ram is an array of arrays
	private final byte[] charom;
	private final byte[][] basic;
	private final byte[][] kernal;
	private final byte[] coloram;	// actually only uses 4 bits per cell, but there is no nybble type...
	private final byte[] scratch = new byte[0x1000];	// used by ultimax mode to swallow writes to invalid addresses
	
	private final byte[][] cpureadmap;	// the view of ram from the cpu (64k) for reads (ram, rom, i/o)
	private final byte[][] cpuwritemap;	// the view of ram from the cpu (64k) for writes (mostly ram)
	private final byte[][] vicreadmap;	// the view of ram from the vic (16k)
	
	public final ObjectProperty<byte[][]> cartLo;	// may or may not be present... 
	public final ObjectProperty<byte[][]> cartHi;
	
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
		ram = new byte[0x10][0x1000];
		coloram = new byte[0x400];
		
		charom = readFile("chargen", 1)[0];	// only need a single chunk
		basic = readFile("basic", 2);
		kernal = readFile("kernal", 2);
		
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
		
		cpureadmap = new byte[0x10][];
		cpuwritemap = new byte[0x10][];
		vicreadmap = new byte[4][];
		
		// Many signals to watch, but they don't change super often, so will just use invalidation listeners-
		// Anything touches them, they're invalidated, then all will be recomputed on the next memory access.
		loram.addListener(p -> cpumapvalid = false);
		hiram.addListener(p -> cpumapvalid = false);
		charen.addListener(p -> cpumapvalid = false);
		ultimax.addListener(p -> {	// if ultimax mode switches, that affects both cpu and vic view of ram
			cpumapvalid = false;
			vicmapvalid = false;
		});
		
		va14.addListener(p -> vicmapvalid = false);
		va15.addListener(p -> vicmapvalid = false);
	}
	
	private byte[][] readFile(String name, int expectedChunks) {
		byte[] contents;
		try (var file = ClassLoader.getSystemClassLoader().getResourceAsStream(name)) {
			if (file.available() == expectedChunks * 0x1000) {
				contents = file.readAllBytes();
			} else {
				// Shouldn't happen...
				throw new Error("File " + name + " is unexpected length.  Aborting.");
			}
		} catch (FileNotFoundException e) {
			// Since it's included in the jar, this should never happen...
			System.err.println("File " + name + " not found.  Aborting.");
			throw new Error(e);	// bail gracelessly
		} catch (IOException e) {
			// could happen... will have to see it happen to decide what to do
			System.err.println("Problem reading file: " + name + ".  Aborting.");
			throw new Error(e);	// TODO recovery?
		}
		var chunks = new byte[expectedChunks][0x1000];
		if (expectedChunks == 1) {
			chunks[0] = contents;
		} else {
			for (int c = 0; c < expectedChunks; c++) {
				chunks[c] = Arrays.copyOfRange(contents, c * 0x1000, (c + 1) * 0x1000);
			}
		}
		return chunks;
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
				// TODO setup memory map
			}
			var ha = getHigh(addr);
			if (ha == 0xd && (ultimax.get() ||	// all ultimax variants have i/o
					!charen.get() && (!hiram.get() || !loram.get()) )) {	// if no i/o, writes go to ram under char rom
				// TODO i/o handling
			} else {
				cpuwritemap[ha][addr & 0xfff] = data;
			}
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
			// TODO setup memory map
		}
		var ha = getHigh(addr);
		if (ha == 0xd && (ultimax.get() ||	// ultimax always has i/o
				!charen.get() ||	// as long as char rom isn't enabled, we see i/o
				(loram.get() && hiram.get() && (!game.get() || exrom.get())))) {	// except for these special cases
			// TODO special i/o handling
		}
		return cpureadmap[ha][addr & 0xfff];
		//assert (addr == (addr & 0xffff)) : "Problem: address " + addr + " out of range."; don't need this for short addr
/*		ByteBuffer buf = null;	// give me exception if I have missed anything
		if (ultimax.get()) {	// lots of stuff is handled differently, so special case
			switch (getHigh(addr)) {
			case 0:
				buf = ram;
				break;
			case 0xd:
				// TODO i/o handling
				break;
			case 0x8:
			case 0x9:
				buf = cartLo.get();
				addr &= 0x1fff;
				break;
			case 0xe:
			case 0xf:
				buf = cartHi.get();
				addr &= 0x1fff;
				break;
			default:	// all others return junk
				return -1;
			}
		} else {
			switch (getHigh(addr)) {
			case 8:
			case 9:
				if (exrom.get() && (!hiram.get() && !loram.get())) {
					buf = cartLo.get();
					addr &= 0x1fff;
				} else {
					buf = ram;
				}
				break;
			case 0xa:
			case 0xb:
				if (game.get() && !hiram.get()) {
					buf = cartHi.get();
					addr &= 0x1fff;
				} else if (!hiram.get() && !loram.get()) {
					buf = basic;
					addr &= 0x1fff;
				} else {
					buf = ram;
				}
				break;
			case 0xd:
				// TODO i/o handling
				break;
			case 0xe:
			case 0xf:
				if (hiram.get()) {
					buf = ram;
				} else {
					buf = kernal;
					addr &= 0x1fff;
				}
				break;
			default:
				buf = ram;
			}
		}
		return buf.get(addr);
*/	}
	
	private void cpumap() {	// setup the cpu's view of memory
		if (ultimax.get()) {
			cpureadmap[0] = ram[0];
			cpuwritemap[0] = ram[0];
			for (int i=1; i<0xd; i++) {	// will add in roms for readmap after
				cpureadmap[i] = scratch;
				cpuwritemap[i] = scratch;
			}
			cpureadmap[8] = cartLo.get()[0];
			cpureadmap[9] = cartLo.get()[1];
			cpureadmap[0xe] = cartHi.get()[0];
			cpureadmap[0xf] = cartHi.get()[1];
		}
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
		return (short) ((coloram[addr & 0x3ff] << 8) | vicreadmap[ha][addr & 0xfff]);
	}
}
