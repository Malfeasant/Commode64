package us.malfeasant.commode64.machine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

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
	private final ByteBuffer ram;
	private final ByteBuffer charom;
	private final ByteBuffer basic;
	private final ByteBuffer kernal;
	private final ByteBuffer coloram;	// actually only uses 4 bits per cell, but there is no nybble type...
	// maybe implement i/o as a custom bytebuffer?  or series of?
	
	public final ObjectProperty<ByteBuffer> cartLo;	// may or may not be present... 
	public final ObjectProperty<ByteBuffer> cartHi;
	
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
	
	public Memory() {
		ram = ByteBuffer.allocate(0x10000);
		coloram = ByteBuffer.allocate(0x400);
		
		charom = ByteBuffer.wrap(readFile("chargen", 0x1000)).asReadOnlyBuffer();
		basic = ByteBuffer.wrap(readFile("basic", 0x2000)).asReadOnlyBuffer();
		kernal = ByteBuffer.wrap(readFile("kernal", 0x2000)).asReadOnlyBuffer();
		
		cartLo = new SimpleObjectProperty<>();
		cartHi = new SimpleObjectProperty<>();
		
		va14 = new SimpleBooleanProperty(false);	// TODO what is true init value?
		va15 = new SimpleBooleanProperty(false);	// TODO what is true init value?
		
		exrom = new SimpleBooleanProperty(false);
		game = new SimpleBooleanProperty(false);
		ultimax = new SimpleBooleanProperty(false);
		ultimax.bind(Bindings.and(game, Bindings.not(exrom)));	// so ultimax will be true if exrom=false and game=true
		
		loram = new SimpleBooleanProperty(false);
		hiram = new SimpleBooleanProperty(false);
		charen = new SimpleBooleanProperty(false);
	}
	
	private byte[] readFile(String name, int expectedBytes) {
		byte[] contents;
		try (var file = ClassLoader.getSystemClassLoader().getResourceAsStream(name)) {
			if (file.available() == expectedBytes) {
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
		return contents;
	}
	
	private int getHigh(int addr) {	// helper function to read top 4 bits of address
		return (addr >> 12) & 0xf;	// need the and, otherwise we end up with negatives...
	}
	/**
	 * Models a write from CPU- almost always goes to RAM, but may need to be intercepted to go to i/o-
	 * unclear whether in ultimax mode writes to 1000-7fff go to ram or go nowhere- easy thing would be to handle writes
	 * normally, but return junk on read- but if ultimax can be entered/exited at will in a running system, that's not
	 * ideal, we might overwrite ram contents that should be preserved...  so this implementation will block writes
	 * until I find proof otherwise.
	 * @param addr - Address to write to
	 * @param data - Data to be written
	 */
	public void poke(short addr, byte data) {
		var ha = getHigh(addr);
		if (ha == 0xd && (ultimax.get() ||	// all ultimax variants have i/o
				!charen.get() && (!hiram.get() || !loram.get()) )) {	// if no i/o, writes go to ram under char rom
			// TODO i/o handling
		} else if (ultimax.get() &&	ha > 0) {	// if not i/o, only first 4k of memory exists in ultimax mode
			// do nothing
		} else {
			ram.put(addr & 0xffff, data);	// need this and otherwise index could be negative...
		}
	}
	
	/**
	 * Models a read from CPU
	 * @param addr - Address to read from
	 * @return - Data read from appropriate source
	 */
	public int peek(int addr) {
		assert (addr == (addr & 0xffff)) : "Problem: address " + addr + " out of range.";
		ByteBuffer buf = null;	// give me exception if I have missed anything
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
	}
	
	/**
	 * Models a fetch from VIC
	 * @param addr - 14-bit address to read from- top 2 bits come from CIA #? TODO which?  and how?
	 * @return - 12-bits of data- top 4 bits come from color ram
	 */
	public int vread(int addr) {
		assert (addr == (addr & 0x3fff)) : "Video read: Address " + addr + " out of range.";
		var buf = ram;
		if (va14.get()) addr |= 0x4000;
		if (va15.get()) addr |= 0x8000;
		switch (getHigh(addr)) {
		case 1:
		case 9:
			buf = charom;
			addr &= 0xfff;
		}
		return (coloram.get(addr & 0x3ff) << 8) | buf.get(addr);
	}
}