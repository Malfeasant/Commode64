package us.malfeasant.commode64.cia.tod;

public enum DigitsByRegister {
	TENTHS {
		@Override
		Digit getLow(DigitSet d) {
			return d.digitMap.get(DigitsByName.T);
		}
		@Override
		Digit getHigh(DigitSet d) {
			return Digit.ZERO;
		}
	}, SECONDS {
		@Override
		Digit getLow(DigitSet d) {
			return d.digitMap.get(DigitsByName.SL);
		}
		@Override
		Digit getHigh(DigitSet d) {
			return d.digitMap.get(DigitsByName.SH);
		}
	}, MINUTES {
		@Override
		Digit getLow(DigitSet d) {
			return d.digitMap.get(DigitsByName.ML);
		}
		@Override
		Digit getHigh(DigitSet d) {
			return d.digitMap.get(DigitsByName.MH);
		}
	}, HOURS {
		@Override
		Digit getLow(DigitSet d) {
			return d.digitMap.get(DigitsByName.HL);
		}
		@Override
		Digit getHigh(DigitSet d) {
			return d.digitMap.get(DigitsByName.HH);
		}
	};
	int read(Time t) {
		return getLow(t.getDigitsForRead()).get() | (getHigh(t.getDigitsForRead()).get() << 4);
	}
	void write(Time t, int i) {
		getLow(t.getDigitsForWrite()).set(i & 0xf);
		getHigh(t.getDigitsForWrite()).set(i >> 4);
	}
	abstract Digit getLow(DigitSet d);
	abstract Digit getHigh(DigitSet d);
}
