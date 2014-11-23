package us.malfeasant.commode64.plumbing;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class Bus {
	private final Map<Tag, Proxy> writers;
	private final int mask;
	
	public Bus(int width) {
		if (width < 1 || width > 30) throw new IllegalArgumentException("Bus width must be positive, max 30");
		mask = (1 << width) - 1;
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
		int value = mask;
		for (Proxy p : writers.values()) {
			value &= p.value;
		}
		return value;
	}
}
