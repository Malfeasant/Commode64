package us.malfeasant.commode64.machine;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Models the address/data buses and related signals- including the video chip's special circuit enabling 12-bit fetches,
 * and char rom in banks 0 and 2
 * @author Malfeasant
 */
public class Memory {
	private final byte[] ram;
	private final byte[] charom;
	private final byte[] basic;
	private final byte[] kernal;
	private final byte[] coloram;	// actually only uses 4 bits per cell, but there is no nybble type...
	
	public Memory() {
		ram = new byte[0x10000];
		coloram = new byte[0x400];
		
		charom = readFile("chargen", 0x1000);
		basic = readFile("basic", 0x2000);
		kernal = readFile("kernal", 0x2000);
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
}
