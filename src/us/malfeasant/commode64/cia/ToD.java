package us.malfeasant.commode64.cia;

class ToD {
	private final PackedTimeField time = new PackedTimeField();
	private PackedTimeField latch = time;
	private final PackedTimeField alarm = new PackedTimeField(0);
	private boolean setAlarm = false;
	
	/**
	 * Override this method to receive an interrupt
	 */
	void setIRQ() {	}

	void set(int addr, int data) {
		Fields f = Fields.values()[addr];
		
		f.pack(this, data);
	}
	int get(int addr) {
		Fields f = Fields.values()[addr];
		return f.unpack(this);
	}
	void tick() {
		if (time.run) {
			switch (time.time & 0xf) {
			case 0x9:
				switch (time.time & 0xf00) {
				case 0x900:
					switch (time.time & 0x7000) {
					case 0x5000:
						switch (time.time & 0xf0000) {
						case 0x90000:
							switch (time.time & 0x700000) {
							case 0x500000:
								switch (time.time & 0x1f000000) {
								
								}
							}
						}
					}
				}
			}
		}
	}	// now I remember why I wanted to break this out into separate fields like HH/HL/MH/ML/SH/SL/T...
	
	/**
	 * So as not to duplicate a bunch of code, Time, Latch, and Alarm are each one of these, then an accessor will
	 * make any modifications.
	 */
	static class PackedTimeField {
		int time;
		boolean run = false;
		public PackedTimeField() {
			this(0x010000);	// time resets to 1 am and not running
		}
		PackedTimeField(int t) {
			time = t;
		}
	}
	
	/**
	 * Accessor for PackedTimeField
	 */
	enum Fields {
		TENTHS(0xf) {
			@Override
			void pack(ToD t, int incoming) {
				super.pack(t, incoming);
				if (!t.setAlarm) {
					t.time.run = true;
				}
			}
			@Override
			int unpack(ToD t) {
				int data = super.unpack(t);
				if (t.latch != t.time) {
					t.latch = t.time;
				}
				return data;
			}
		}, SECONDS, MINUTES,
		HOURS(0x9f) {
			@Override
			void pack(ToD t, int incoming) {
				if (!t.setAlarm) {
					t.time.run = false;
				}
				super.pack(t, incoming);
			}
			@Override
			int unpack(ToD t) {
				if (t.latch == t.time) {
					t.latch = new PackedTimeField(t.time.time);
				}
				return super.unpack(t);
			}
		};
		private final int mask;
		Fields() {
			this(0x7f);
		}
		Fields(int m) {
			mask = m;
		}
		int unpack(ToD t) {
			return (t.latch.time >> (ordinal() * 8)) & mask;
		}
		void pack(ToD t, int incoming) {
			incoming &= mask;	// only keep bits valid for this field
			(t.setAlarm ? t.alarm : t.time).time &= ~(0xff << ordinal() * 8);	// mask out the byte we are about to change
			(t.setAlarm ? t.alarm : t.time).time |= incoming << ordinal() * 8;
		}
	}
}
