package us.malfeasant.commode64.plumbing;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class Signal extends Entity {
	private final Map<Port.W, Integer> writers = new IdentityHashMap<>();
	private final Set<Port.R> readers = new HashSet<>();
	private final int width;
	public Signal(int width) {
		if (width < 0 || width > 30) throw new IllegalArgumentException();
		this.width = width;
	}
	private class Reader extends Entity implements Port.R {
		private int localView = -1;
		@Override
		public int read() {
			return localView;
		}
	}
	public Port.R getReader() {
		final Port.R reader = new Reader();
		enqueue(new Runnable() {
			@Override
			public void run() {
				readers.add(reader);
			}
		});
		return reader;
	}
	
	private class Writer extends Entity implements Port.W {
		private final int offset;
		private final int mask;
		Writer(int offset, int width) {
			if (offset < 0 || width < 0 || offset + width > Signal.this.width)
				throw new IllegalArgumentException();
			this.offset = offset;
			mask = (1 << width) - 1;
		}
		@Override
		public void write(int w) {
			writeFrom(this, (w & mask) << offset);
		}
		@Override
		public void release() {
			writeFrom(this, -1);
		}
	}
	private void writeFrom(final Port.W writer, final int w) {
		enqueue(new Runnable() {
			@Override
			public void run() {
				Integer i = writers.put(writer, w);
				if (i == null || i != w) ;	// TODO notify readers
			}
		});
	}
}
