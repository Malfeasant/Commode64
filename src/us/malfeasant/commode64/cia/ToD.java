package us.malfeasant.commode64.cia;

import java.util.Arrays;

class ToD {
	private final byte[] time = new byte[4];
	private byte[] latch = time;
	private final byte[] alarm = new byte[4];
	private boolean run;
	private boolean irq;
	
	ToD() {
		Fields.HOURS.set(time, 0x1);	// time resets to 1 am and not running
	}
	
	/**
	 * Reading IRQ clears it
	 * @return
	 */
	boolean getIRQ() {
		boolean r = irq;
		irq = false;
		return r;
	}
	void set(int addr, int data, boolean alrm) {
		Fields f = Fields.values()[addr];
		if (!alrm && (f.equals(Fields.HOURS))) {
			run = false;
		}
		f.set(alrm ? alarm : time, data);
		if (!alrm && (f.equals(Fields.TENTHS))) {
			run = true;
		}
	}
	int get(int addr) {
		Fields f = Fields.values()[addr];
		int data;
		if (f.equals(Fields.HOURS)) {
			latch = Arrays.copyOf(time, time.length);
		}
		data = f.get(latch);
		if (f.equals(Fields.TENTHS)) {
			latch = time;
		}
		return data;
	}
	void tick() {
		if (run) {
			Fields.TENTHS.tick(this);
			irq |= Arrays.equals(time, alarm);
		}
	}
	/**
	 * Dumps all fields without affecting latch
	 */
	String debug() {
		StringBuilder t = new StringBuilder("\t Time = ");
		StringBuilder l = new StringBuilder("\tLatch = ");
		StringBuilder a = new StringBuilder("\tAlarm = ");
		for (Fields f : Fields.values()) {
			t.append(String.format("%x ", f.get(time)));
			l.append(String.format("%x ", f.get(latch)));
			a.append(String.format("%x ", f.get(alarm)));
		}
		return t.toString() + "\n" + l.toString() + "\n" + a.toString() + "\n\t  irq = " + irq;
	}
	private enum Fields {
		TENTHS(0xf), SECONDS, MINUTES,
		HOURS(0x9f) {
			@Override
			void tick(ToD t) {
				int h = get(t.time);
				int l = h & 0xf;
				boolean pm = (h & 0x80) != 0;
				h >>= 4;
				h &= 1;
				if (h == 0) {
					if (l == 9) {
						h = 1;
						l = 0;
					} else {
						l++;
					}
				} else {
					if (l == 1) {
						l = 2;
						pm = !pm;
					} else if (l == 2) {
						h = 0;
						l = 1;
					} else {
						l++;
					}
				}
				set(t.time, (pm ? 0x80 : 0) | ((h & 1) << 4) | (l & 0xf));
			}
		};
		private final int mask;
		private final int compareLo;
		private final int compareHi;
		Fields() {
			this(0x7f, 5, 9);
		}
		Fields(int m) {
			this(m, 0, 9);
		}
		Fields(int m, int h, int l) {
			mask = m;
			compareLo = l;
			compareHi = h;
		}
		void tick(ToD t) {
			int l = get(t.time);
			int h = l >> 4;
			l &= 0xf;
			if (l == compareLo) {
				l = 0;
				if (h == compareHi) {
					h = 0;
					values()[ordinal() + 1].tick(t);
				} else {
					h++;
				}
			} else {
				l++;
			}
			set(t.time, (h << 4) | (l & 0xf));
		}
		int get(byte[] b) {
			return b[ordinal()] & mask;
		}
		void set(byte[] b, int d) {
			b[ordinal()] = (byte) (d & mask);
		}
	}
}
