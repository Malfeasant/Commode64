package us.malfeasant.commode64.machine.memory;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Models a 4k chunk of memory space.  Will be subclassed by RAM, ROM, I/O, others?
 * @author Malfeasant
 */
public abstract class Chunk {
	final byte[] contents;
	final int offset;
	
	/**
	 * Construct a chunk backed by an existing array larger than 4k.  Intended to be used for ROM files larger than 4k.
	 * @param contents The backing array- must be at least 4k, is expected to be a multiple of 4k
	 * @param offset Where in the existing array this chunk's view of it starts at- handy for larger than 4k ROMs.
	 */
	protected Chunk(byte[] contents, int offset) {
		this.contents = contents;
		this.offset = offset;
	}
	byte peek(short addr) {
		return contents[maskBits(addr) + offset];
	}
	void poke(short addr, byte data) {
		contents[maskBits(addr) + offset] = data;
	}
	protected short maskBits(short addr) {
		return (short) (addr & 0xfff);	// ensures no out-of-range writes
	}
	
	/**
	 * Builds the full 64k of system RAM- backed by a single array to make loading/saving contents easier.
	 * @return An array of Chunks that model ordinary RAM
	 */
	public static RAM[] ram() {
		var ramBytes = new byte[0x10000];
		var ramChunks = new RAM[0x10];
		for (int i = 0; i < ramChunks.length; i++) {
			ramChunks[i] = new RAM(ramBytes, i << 12);
		}
		return ramChunks;
	}
	public static ROM[] basic() {
		return fromIncludedFile("basic", 2);
	}
	public static ROM[] kernal() {
		return fromIncludedFile("kernal", 2);
	}
	public static ROM charrom() {
		return fromIncludedFile("chargen", 1)[0];
	}
	// TODO - make the following more general purpose for loading external files (cartridges? alternate kernal/basic?)
	private static ROM[] fromIncludedFile(String name, int expectedChunks) {
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
		var rom = new ROM[expectedChunks];
		for (int i=0; i < expectedChunks; i++) {
			rom[i] = new ROM(contents, i * 0x1000);
		}
		return rom;
	}
}
