package us.malfeasant.commode64.cia.tod;

import java.util.EnumMap;
import java.util.Map;

public class DigitSet {
	
	Map<DigitsByName, Digit> digitMap = new EnumMap<>(DigitsByName.class);
	
	public DigitSet() {
		for (DigitsByName name : DigitsByName.values()) {
			digitMap.put(name, new Digit(name));
		}
	}
	DigitSet(DigitSet other) {
		for (DigitsByName name : DigitsByName.values()) {
			Digit d = new Digit(name);
			d.set(other.digitMap.get(name).get());
			digitMap.put(name, d);
		}
	}
	void inc() {
		if (digitMap.get(DigitsByName.T).inc() && 
			digitMap.get(DigitsByName.SL).inc() && 
			digitMap.get(DigitsByName.SH).inc() && 
			digitMap.get(DigitsByName.ML).inc() && 
			digitMap.get(DigitsByName.MH).inc()) {	// if we get this far, minutes has rolled from 59 to 00
			Digit hl = digitMap.get(DigitsByName.HL);
			Digit hh = digitMap.get(DigitsByName.HH);
			if ((hh.get() & 1) == 0) {
				if (hl.get() == 9) {
					hh.inc();
					hl.set(0);
				} else {
					hl.inc();
				}
			} else {
				if (hl.get() == 1) {
					hl.set(2);
					hh.set(hh.get() ^ 0x80);	// am/pm flag toggles when 11 rolls to 12
				} else if (hl.get() == 2) {	// and 12 rolls to 01
					hh.set(hh.get() & 8);	// preserve pm flag, but set digit to 0 
					hl.set(1);
				} else {
					hl.inc();
				}
			}
		}
	}
	DigitSet copy() {
		return new DigitSet(this);
	}
}
