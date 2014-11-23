package us.malfeasant.commode64.plumbing;

import us.malfeasant.commode64.plumbing.AddressBus.Proxy;

public enum Tag {
	CPU, PORT,
	VIC {
		@Override
		Proxy makeNew(AddressBus bus) {
			return bus.new Proxy() {
				@Override
				public void write(int v) {
					super.write(v | 0xc000);
				}
				@Override
				public int read() {
					return super.read() & 0x3f;
				}
			};
		}
	},
	BANK {
		@Override
		Proxy makeNew(AddressBus bus) {
			return bus.new Proxy() {
				@Override
				public void write(int v) {
					super.write((v << 14) | 0x3fff);
				}
				@Override
				public int read() {	// this likely will never get used, but just to be complete...
					return super.read() >> 14;
				}
			};
		}
	};
	Proxy makeNew(AddressBus bus) {
		return bus.new Proxy();
	}
}