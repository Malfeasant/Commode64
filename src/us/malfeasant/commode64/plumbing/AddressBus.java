package us.malfeasant.commode64.plumbing;

public class AddressBus {
	private final CpuProxy cP = new CpuProxy();
	private final VicProxy vP = new VicProxy();
	private final BankProxy bP = new BankProxy();
	private final PortProxy pP = new PortProxy();
	
	public AddressBus() {
		
	}
	
	public class Proxy {
		int value;
		public void write(int v) {
			value = v & 0xffff;
		}
	}
	public class VicProxy extends Proxy {
		@Override
		public void write(int v) {
			super.write(v & 0x3fff);
		}
		public int read() {
			return get() & 0x3f;
		}
	}
	public class BankProxy extends Proxy {
		@Override
		public void write(int v) {
			super.write(v << 14);
		}
	}
	public class CpuProxy extends Proxy {}
	public class PortProxy extends Proxy {
		public int read() {
			return get();
		}
	}
	
	private int get() {
		int value = 0xffff;
		value &= cP.value;
		value &= (vP.value | bP.value);
		value &= pP.value;
		return value;
	}
}
