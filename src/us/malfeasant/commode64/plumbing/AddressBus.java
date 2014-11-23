package us.malfeasant.commode64.plumbing;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class AddressBus {
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
	private final Map<Tag, Proxy> writers;
	
	public AddressBus() {
		Map<Tag, Proxy> w = new EnumMap<>(Tag.class);
		for (Tag t : Tag.values()) {
			w.put(t, t.makeNew(this));
		}
		writers = Collections.unmodifiableMap(w);
	}
	
	public class Proxy {
		int value;
		public void write(int v) {
			value = v & 0xffff;
		}
		public int read() {
			return get();
		}
	}
	
	private int get() {
		int value = 0xffff;
		for (Proxy p : writers.values()) {
			value &= p.value;
		}
		return value;
	}
}
