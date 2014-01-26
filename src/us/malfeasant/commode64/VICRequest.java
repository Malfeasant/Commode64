package us.malfeasant.commode64;

public class VICRequest {
	public final int address;
	public final boolean busAvailable;
	public VICRequest(int addr, boolean ba) {
		address = addr & 0x3fff;
		busAvailable = ba;
	}
}
