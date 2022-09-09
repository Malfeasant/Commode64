package us.malfeasant.commode64.machine.video;
// TODO I think I want to split these- so each item has a firsthalf and secondhalf cycle- and only call secondhalf if
// ba has gone to 0 so ok to steal a cycle... and rather than doing the fetches directly, they should set an address
// variable in the passed video object, then let it do the fetch- otherwise no way to modify addr in case of ecm...
/**
 * Let's try as a state machine... 
 * http://www.unusedino.de/ec64/technical/misc/vic656x/vic656x.html has been crucial in figuring out how to model
 * bus cycles- but it has one weakness.  It considers the "beginning" of a line to be when the IRQ from a raster
 * interrupt is triggered- but this shifts around the special cycles between the different variants (NTSC/PAL old
 * and new).  It is more likely that there is no "cycle number" counter in the chip at all, or if there is, it has
 * there is no reason that it increments the line at 0- in fact the 6567's datasheet-
 * http://archive.6502.org/datasheets/mos_6567_vic_ii_preliminary.pdf pretty much confirms that the "Increment
 * vertical counter" is based on a pixel x-position near to the "end" of the line.  The fact that the IRQ interrupt
 * comes one cycle later in raster line 0 backs up this hypothesis- the line is incremented, it goes one beyond its
 * max, then on the next cycle, it is corrected to 0, and the raster compare IRQ is dead simple.  So, long story
 * short, I have shifted the beginning of the cycle to when the sprite 0 fetch happens- then the sequence is the
 * same for all variants up until the last few cycles, which are all idle cycles- 6569 has 2, 6567R56A (old NTSC)
 * has 3, and 6567R8 (new NTSC) has 4. 
 * 
 * @author Malfeasant
 */
public enum CycleType {
	S0P {
		@Override
		void advance(Video v) {
			v.spriteCounter = (v.memoryProperty.get().vread(v.vmbase + 0x3f8)) << 6;
			// If enabled, steal a cpu cycle for first byte fetch.
			if ((v.preBA == 0) && v.sprites[0].enabled) {
				v.sprites[0].sequencer = (v.memoryProperty.get().vread(v.spriteCounter)) << 16;
			}
			v.spriteCounter++;	// increment 
		}
	},
	S0S {
		@Override
		void advance(Video v) {
			// If enabled, fetch second byte, else idle
			if ((v.preBA == 0) && v.sprites[0].enabled) {
				v.sprites[0].sequencer |= (v.memoryProperty.get().vread(v.spriteCounter)) << 8;
			} else {
				v.memoryProperty.get().vread(0x3fff);	// idle
			}
			v.spriteCounter++;	// increment 
			// If enabled, steal a cpu cycle for third byte fetch.
			if ((v.preBA == 0) && v.sprites[0].enabled) {
				v.sprites[0].sequencer |= v.memoryProperty.get().vread(v.spriteCounter);
			}
			v.spriteCounter++;	// increment 
			// Check if Sprite 2 enabled, if so negate BA
		}
	},
	S1P {
		@Override
		void advance(Video v) {
			v.spriteCounter = v.memoryProperty.get().vread(v.vmbase + 0x3f9);
			// If enabled, steal a cpu cycle for first byte fetch.
		}
	},
	S1S {
		@Override
		void advance(Video v) {
			// TODO If enabled, fetch second byte, else idle
			// If enabled, steal a cpu cycle for third byte fetch.
			// Check if Sprite 3 enabled, if so negate BA
		}
	},
	S2P {
		@Override
		void advance(Video v) {
			v.spriteCounter = v.memoryProperty.get().vread(v.vmbase + 0x3fa);
			// If enabled, steal a cpu cycle for first byte fetch.
		}
	},
	S2S {
		@Override
		void advance(Video v) {
			// TODO If enabled, fetch second byte, else idle
			// If enabled, steal a cpu cycle for third byte fetch.
			// Check if Sprite 4 enabled, if so negate BA
		}
	},
	S3P {
		@Override
		void advance(Video v) {
			v.spriteCounter = v.memoryProperty.get().vread(v.vmbase + 0x3fb);
			// If enabled, steal a cpu cycle for first byte fetch.
		}
	},
	S3S {
		@Override
		void advance(Video v) {
			// TODO If enabled, fetch second byte, else idle
			// If enabled, steal a cpu cycle for third byte fetch.
			// Check if Sprite 5 enabled, if so negate BA
		}
	},
	S4P {
		@Override
		void advance(Video v) {
			v.spriteCounter = v.memoryProperty.get().vread(v.vmbase + 0x3fc);
			// If enabled, steal a cpu cycle for first byte fetch.
		}
	},
	S4S {
		@Override
		void advance(Video v) {
			// TODO If enabled, fetch second byte, else idle
			// If enabled, steal a cpu cycle for third byte fetch.
			// Check if Sprite 6 enabled, if so negate BA
		}
	},
	S5P {
		@Override
		void advance(Video v) {
			v.spriteCounter = v.memoryProperty.get().vread(v.vmbase + 0x3fd);
			// If enabled, steal a cpu cycle for first byte fetch.
		}
	},
	S5S {
		@Override
		void advance(Video v) {
			// TODO If enabled, fetch second byte, else idle
			// If enabled, steal a cpu cycle for third byte fetch.
			// Check if Sprite 7 enabled, if so negate BA
		}
	},
	S6P {
		@Override
		void advance(Video v) {
			v.spriteCounter = v.memoryProperty.get().vread(v.vmbase + 0x3fe);
			// If enabled, steal a cpu cycle for first byte fetch.
		}
	},
	S6S {
		@Override
		void advance(Video v) {
			// TODO If enabled, fetch second byte, else idle
			// If enabled, steal a cpu cycle for third byte fetch.
		}
	},
	S7P {
		@Override
		void advance(Video v) {
			v.spriteCounter = v.memoryProperty.get().vread(v.vmbase + 0x3ff);
			// If enabled, steal a cpu cycle for first byte fetch.
		}
	},
	S7S {
		@Override
		void advance(Video v) {
			// TODO If enabled, fetch second byte, else idle
			// If enabled, steal a cpu cycle for third byte fetch.
		}
	},
	R0 {
		@Override
		void advance(Video v) {
			// TODO refresh cycle, inc counter
		}
	},
	R1 {
		@Override
		void advance(Video v) {
			// TODO refresh cycle, inc counter
			// check if badline coming- if so, negate BA
		}
	},
	R2 {
		@Override
		void advance(Video v) {
			// TODO refresh cycle, inc counter
		}
	},
	R3 {
		@Override
		void advance(Video v) {
			// TODO refresh cycle, inc counter
		}
	},
	R4 {
		@Override
		void advance(Video v) {
			// TODO refresh cycle, inc counter
			// if badline, steal cycle for c fetch
		}
	},
	G00 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G01 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G02 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G03 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G04 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G05 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G06 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G07 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G08 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G09 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G10 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G11 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G12 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G13 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G14 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G15 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G16 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G17 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G18 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G19 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G20 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G21 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G22 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G23 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G24 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G25 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G26 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G27 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G28 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G29 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G30 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G31 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G32 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G33 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G34 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G35 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G36 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G37 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G38 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if badline, steal cycle for c fetch
		}
	},
	G39 {
		@Override
		void advance(Video v) {
			// TODO g fetch
			// if 6569 & Sprite 0 enabled, negate BA
		}
	},
	I0 {
		@Override
		void advance(Video v) {
			// TODO Idle fetch
			// if 6567R56A & Sprite 0 enabled, negate BA
		}
	},
	I1 {
		@Override
		void advance(Video v) {
			// TODO Idle fetch
			// if 6567R8 & Sprite 0 enabled, negate BA
			// if 6569 & Sprite 1 enabled, negate BA
		}
	},
	I2 {	// 6569 does not include this
		@Override
		void advance(Video v) {
			// TODO Idle fetch
			// if 6567R56A & Sprite 1 enabled, negate BA
		}
	},
	I3 {	// 6569, 6567R56A do not include this
		@Override
		void advance(Video v) {
			// TODO Idle fetch
			// I3: if 6567R8 & Sprite 1 enabled, negate BA
		}
	},
	;
	/**
	 * Inversion of control- most of state is kept in Video class, what changes is the action needing to be done.
	 * @param v a video instance to modify
	 */
	abstract void advance(Video v);
	private CycleType nextFor(Variant v) {
		return (ordinal() >= v.endOfLine) ? values()[0] : values()[ordinal() + 1];
	}
	private CycleType ahead3(Variant v) {
		var ord = ordinal() + 3;
		if (ord > v.endOfLine) {
			ord -= v.endOfLine + 1; // again because this is last cycle from 0, not number of
		}
		return values()[ord];
	}
}
