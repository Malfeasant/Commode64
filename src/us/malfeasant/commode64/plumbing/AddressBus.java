package us.malfeasant.commode64.plumbing;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class AddressBus {
	private final Map<Tag, Proxy> writers;
	
	public AddressBus() {
		Map<Tag, Proxy> w = new EnumMap<>(Tag.class);
		for (Tag t : Tag.values()) {
			w.put(t, t.makeNew(this));
		}
		writers = Collections.unmodifiableMap(w);
	}
	public Proxy getProxy(Tag which) {
		return writers.get(which);
	}
	
	public class Proxy {
		int value = -1;
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
