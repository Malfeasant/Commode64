package us.malfeasant.commode64;

public class CPURequest {
	public final int address;
	public final int data;
	public final boolean write;
	public CPURequest(int addr, int data, boolean wr) {
		address = addr & 0xffff;
		this.data = data & 0xff;
		write = wr;
	}
}
