package us.malfeasant.commode64.machine.memory;

public class RAM extends Chunk {
	RAM() {
		super(new byte[0x1000], 0);
	}
}
