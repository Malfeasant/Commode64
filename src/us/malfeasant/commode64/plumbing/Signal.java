package us.malfeasant.commode64.plumbing;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Signal {
	private final Executor sigExec = Executors.newSingleThreadExecutor();
	private final Map<Writer, Integer> writers = new IdentityHashMap<>();
	private final Set<Reader> readers = new HashSet<>();
	private final int width;
	private int cache = -1;
	public Signal(int width) {
		if (width < 0 || width > 30) throw new IllegalArgumentException();
		this.width = width;
	}
	private class PortBase implements Port {
		protected final int offset;
		protected final int mask;
		private PortBase(int offset, int width) {
			if (offset < 0 || width < 0 || offset + width > Signal.this.width)
				throw new IllegalArgumentException();
			this.offset = offset;
			mask = ((1 << width) - 1) << offset;
		}
	}
	private class Reader extends PortBase implements Port.R {
		private final Executor portExec = Executors.newSingleThreadExecutor();
		private Reader(int offset, int width) {
			super(offset, width);
		}
		private int localView = -1;
		@Override
		public int read() {
			return localView;
		}
		private void update(final int in) {
			portExec.execute(new Runnable() {
				@Override
				public void run() {
					localView = (in & mask) >> offset;
				}
			});
		}
	}
	public Port.R getReader() {
		return getReader(0, width);
	}
	public Port.R getReader(int offset, int width) {
		final Reader reader = new Reader(offset, width);
		sigExec.execute(new Runnable() {
			@Override
			public void run() {
				readers.add(reader);
			}
		});
		return reader;
	}
	
	private class Writer extends PortBase implements Port.W {
		private Writer(int offset, int width) {
			super(offset, width);
		}
		@Override
		public void write(int w) {
			writeFrom(this, ((w << offset) & mask) | ~mask);
		}
		@Override
		public void release() {
			writeFrom(this, -1);
		}
	}
	public Port.W getWriter() {
		return getWriter(0, width);
	}
	public Port.W getWriter(int offset, int width) {
		return new Writer(offset, width);
	}
	private void writeFrom(final Writer writer, final int w) {
		sigExec.execute(new Runnable() {
			@Override
			public void run() {
				Integer old = writers.put(writer, w);
				if (old == null || old != w) {
					int working = -1;
					for (int v : writers.values()) {
						working &= v;
					}
					if (cache != working) {
						cache = working;
						for (Reader r : readers) {
							r.update(working);
						}
					}
				}
			}
		});
	}
	private class ReadWrite implements Port.RW {
		private final Reader reader;
		private final Writer writer;
		private ReadWrite(int offset, int width) {
			reader = new Reader(offset, width);
			writer = new Writer(offset, width);
		}
		@Override
		public int read() {
			return reader.read();
		}
		@Override
		public void write(int w) {
			writer.write(w);
		}
		@Override
		public void release() {
			writer.release();
		}
	}
	public Port.RW getReadWrite() {
		return getReadWrite(0, width);
	}
	public Port.RW getReadWrite(int offset, int width) {
		return new ReadWrite(offset, width);
	}
}
