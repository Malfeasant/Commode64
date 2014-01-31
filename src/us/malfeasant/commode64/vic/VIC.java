package us.malfeasant.commode64.vic;

import us.malfeasant.commode64.CPURequest;
import us.malfeasant.commode64.VICRequest;

public abstract class VIC {
	private final RegisterBank regs = new RegisterBank();
	private final Revision rev;
	
	public VIC(Revision r) {
		if (r == null) throw new NullPointerException();
		rev = r;
	}
	
	public int regAccess(CPURequest request) {
		int addr = request.address & 0x3f;
		return regs.access(addr, request.data, request.write);
	}
	
	private int cycle;
	private int line;
	private boolean active;	// true when within display window, false outside
	private int vc;	// video counter
	private int vcbase;
	private int rc;	// row counter
	private final int[] lineBuffer = new int[40];	// holds text characters for multiple lines 
	private int vmli;	// index into the above
	private boolean ba;	// halts the cpu when we need to steal cycles
	private boolean badLine;	// true means vic will steal cycles from cpu
	
	public void tick() {
		
	}
	private void cCycle() {	// always the same no matter what mode, but only happens every 8 lines
		int addr = regs.vmBase | vc;
		lineBuffer[vmli] = sendRequest(new VICRequest(addr, ba));
	}
	private void gCycle() {	// happens every line, mode affects interpretation
		int mode = (regs.extEn ? 4 : 0) | (regs.bmEn ? 2 : 0) | (regs.mcEn ? 1 : 0);
		int text = lineBuffer[vmli] & 0xff;
		int cram = lineBuffer[vmli] >> 8;
		int addr;
		if (active) {
			addr = regs.charBase;
			if (regs.bmEn) {
				addr &= 0x2000;
				addr |= vc << 3;
			} else {
				addr |= text << 3;
			}
			addr |= rc;
		} else {
			addr = -1;
		}
		if (regs.extEn) {
			addr &= ~0x600;
		}
		int data = sendRequest(new VICRequest(addr, ba));
		
	}
	protected abstract int sendRequest(VICRequest req);
}
