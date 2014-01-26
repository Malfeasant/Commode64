package us.malfeasant.commode64.vic;

import us.malfeasant.commode64.CPURequest;

public class VIC {
	private final RegisterBank regs = new RegisterBank();
	public VIC() {
	}
	public int regAccess(CPURequest request) {
		int index = request.address & 0x3f;
		return regs.access(index, request.data, request.write);
	}
}
