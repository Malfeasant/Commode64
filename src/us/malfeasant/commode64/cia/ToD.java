package us.malfeasant.commode64.cia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ToD {
	private final byte[] time = new byte[4];
	private byte[] latch = time;
	private final byte[] alarm = new byte[4];
	private boolean run;
	private final List<ChangeListener> listeners = new ArrayList<>();
	
	ToD() {
		Fields.HOURS.set(time, 0x1);	// time resets to 1 am and not running
	}
	
	/**
	 * Override this method
	 */
	void setIRQ() {
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
		if (alrm) {
			updateAlarm();
		} else {
			updateTime();
		}
	}
	int get(int addr) {
		Fields f = Fields.values()[addr];
		int data;
		if (f.equals(Fields.HOURS) && latch == time) {
			latch = Arrays.copyOf(time, time.length);
			updateLatch();
		}
		data = f.get(latch);
		if (f.equals(Fields.TENTHS)) {
			latch = time;
			updateLatch();
		}
		return data;
	}
	void tick() {
		if (run) {
			if (Fields.TENTHS.tick(this)) setIRQ();
			updateTime();
		}
	}
	public void addListener(ChangeListener l) {
		listeners.add(l);
	}
	public void removeListener(ChangeListener l) {
		listeners.remove(l);
	}
	private void updateAlarm() {
		for (ChangeListener l : listeners) {
			l.updateAlarm(alarm[3], alarm[2], alarm[1], alarm[0]);
		}
	}
	private void updateTime() {
		for (ChangeListener l : listeners) {
			l.updateTime(time[3], time[2], time[1], time[0]);
		}
	}
	private void updateLatch() {
		for (ChangeListener l : listeners) {
			l.updateLatch(latch[3], latch[2], latch[1], latch[0]);
		}
	}
	
	/*
	String debug() {
		StringBuilder t = new StringBuilder("\t Time = ");
		StringBuilder l = new StringBuilder("\tLatch = ");
		StringBuilder a = new StringBuilder("\tAlarm = ");
		for (Fields f : Fields.values()) {
			t.append(String.format("%x ", f.get(time)));
			l.append(String.format("%x ", f.get(latch)));
			a.append(String.format("%x ", f.get(alarm)));
		}
		return t.toString() + "\n" + l.toString() + "\n" + a.toString();
	}*/
	enum Fields {
		TENTHS(0xf), SECONDS, MINUTES,
		HOURS(0x9f) {
			@Override
			boolean tick(ToD t) {
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
				return get(t.time) == get(t.alarm);
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
		boolean tick(ToD t) {
			boolean match = true;
			int l = get(t.time);
			int h = l >> 4;
			l &= 0xf;
			if (l == compareLo) {
				l = 0;
				if (h == compareHi) {
					h = 0;
					match &= values()[ordinal() + 1].tick(t);
				} else {
					h++;
				}
			} else {
				l++;
			}
			set(t.time, (h << 4) | (l & 0xf));
			return match && (get(t.time) == get(t.alarm));
		}
		int get(byte[] b) {
			return b[ordinal()] & mask;
		}
		void set(byte[] b, int d) {
			b[ordinal()] = (byte) (d & mask);
		}
	}
}
