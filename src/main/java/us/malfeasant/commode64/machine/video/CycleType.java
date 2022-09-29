package us.malfeasant.commode64.machine.video;
// TODO I think I want to split these- so each item has a firsthalf and secondhalf cycle- and only call secondhalf if
// ba has gone to 0 so ok to steal a cycle... and rather than doing the fetches directly, they should set an address
// variable in the passed video object, then let it do the fetch- otherwise no way to modify addr in case of ecm...
/**
 * Let's try as a state machine... 
 * http://www.unusedino.de/ec64/technical/misc/vic656x/vic656x.html has been crucial in figuring out how to model
 * bus cycles- but it has one weakness.  It considers the "beginning" of a line to be when the IRQ from a raster
 * interrupt is triggered- but this shifts around the special cycles between the different variants (NTSC/PAL old
 * and new).  It is more likely that there is no "cycle number" counter in the chip at all, or if there is, there
 * is no reason that it increments the line at 0- in fact the 6567's datasheet-
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
	S0P {	// "cycle 58"- all sprites' mcount are reset from mcbase, also checked for display conditions
		@Override
		void clockLo(Video v) {
			for (Sprite s : v.sprites) {
				s.mcount = s.mcbase;
				if (s.dma && s.y == (v.rasterCurrent & 0xff)) {
					s.display = true;
				}
			}
			// fetch sprite pointer
			spfetch(v, 0);
		}

		@Override
		void clockHi(Video v) {
			// If enabled, steal a cpu cycle for first byte fetch.
			sdfetch1(v, 0);
		}
	},
	S0S {
		@Override
		void clockLo(Video v) {
			// If enabled, fetch second byte, else idle
			sdfetch2(v, 0);
			// Check if Sprite 2 enabled, if so negate BA
			if (v.sprites[2].enabled) v.prepareStun(StunSource.SPRITE2);
		}

		@Override
		void clockHi(Video v) {
			// If enabled, steal a cpu cycle for third byte fetch.
			sdfetch3(v, 0);
		}
	},
	S1P {
		@Override
		void clockLo(Video v) {
			// release ba for sprite 0
			v.releaseStun(StunSource.SPRITE0);
			// fetch sprite pointer
			spfetch(v, 1);
		}

		@Override
		void clockHi(Video v) {
			// If enabled, steal a cpu cycle for first byte fetch.
			sdfetch1(v, 1);
		}
	},
	S1S {
		@Override
		void clockLo(Video v) {
			// If enabled, fetch second byte, else idle
			sdfetch2(v, 0);
			// Check if Sprite 3 enabled, if so negate BA
			if (v.sprites[3].enabled) v.prepareStun(StunSource.SPRITE3);
		}

		@Override
		void clockHi(Video v) {
			// If enabled, steal a cpu cycle for third byte fetch.
			sdfetch3(v, 1);
		}
	},
	S2P {
		@Override
		void clockLo(Video v) {
			// release ba for sprite 1
			v.releaseStun(StunSource.SPRITE1);
			// fetch sprite pointer
			spfetch(v, 2);
		}

		@Override
		void clockHi(Video v) {
			// If enabled, steal a cpu cycle for first byte fetch.
			sdfetch1(v, 2);
		}
	},
	S2S {
		@Override
		void clockLo(Video v) {
			// If enabled, fetch second byte, else idle
			sdfetch2(v, 2);
			// Check if Sprite 4 enabled, if so negate BA
			if (v.sprites[4].enabled) v.prepareStun(StunSource.SPRITE4);
		}

		@Override
		void clockHi(Video v) {
			// If enabled, steal a cpu cycle for third byte fetch.
			sdfetch3(v, 2);
		}
	},
	S3P {
		@Override
		void clockLo(Video v) {
			// release ba for sprite 2
			v.releaseStun(StunSource.SPRITE2);
			// fetch sprite pointer
			spfetch(v, 3);
		}

		@Override
		void clockHi(Video v) {
			// If enabled, steal a cpu cycle for first byte fetch.
			sdfetch1(v, 3);
		}
	},
	S3S {
		@Override
		void clockLo(Video v) {
			// If enabled, fetch second byte, else idle
			sdfetch2(v, 3);
			// Check if Sprite 5 enabled, if so negate BA
			if (v.sprites[5].enabled) v.prepareStun(StunSource.SPRITE5);
		}

		@Override
		void clockHi(Video v) {
			// If enabled, steal a cpu cycle for third byte fetch.
			sdfetch3(v, 3);
		}
	},
	S4P {
		@Override
		void clockLo(Video v) {
			// release ba for sprite 3
			v.releaseStun(StunSource.SPRITE3);
			// fetch sprite pointer
			spfetch(v, 4);
		}

		@Override
		void clockHi(Video v) {
			// If enabled, steal a cpu cycle for first byte fetch.
			sdfetch1(v, 4);
		}
	},
	S4S {
		@Override
		void clockLo(Video v) {
			// If enabled, fetch second byte, else idle
			sdfetch2(v, 4);
			// Check if Sprite 6 enabled, if so negate BA
			if (v.sprites[6].enabled) v.prepareStun(StunSource.SPRITE6);
		}

		@Override
		void clockHi(Video v) {
			// If enabled, steal a cpu cycle for third byte fetch.
			sdfetch3(v, 4);
		}
	},
	S5P {
		@Override
		void clockLo(Video v) {
			// release ba for sprite 4
			v.releaseStun(StunSource.SPRITE4);
			// fetch sprite pointer
			spfetch(v, 5);
		}

		@Override
		void clockHi(Video v) {
			// If enabled, steal a cpu cycle for first byte fetch.
			sdfetch1(v, 5);
		}
	},
	S5S {
		@Override
		void clockLo(Video v) {
			// If enabled, fetch second byte, else idle
			sdfetch2(v, 5);
			// Check if Sprite 7 enabled, if so negate BA
			if (v.sprites[6].enabled) v.prepareStun(StunSource.SPRITE7);
		}

		@Override
		void clockHi(Video v) {
			// If enabled, steal a cpu cycle for third byte fetch.
			sdfetch3(v, 5);
		}
	},
	S6P {
		@Override
		void clockLo(Video v) {
			// release ba for sprite 5
			v.releaseStun(StunSource.SPRITE5);
			// fetch sprite pointer
			spfetch(v, 6);
		}

		@Override
		void clockHi(Video v) {
			// If enabled, steal a cpu cycle for first byte fetch.
			sdfetch1(v, 6);
		}
	},
	S6S {
		@Override
		void clockLo(Video v) {
			// If enabled, fetch second byte, else idle
			sdfetch2(v, 6);
		}

		@Override
		void clockHi(Video v) {
			// If enabled, steal a cpu cycle for third byte fetch.
			sdfetch3(v, 6);
		}
	},
	S7P {
		@Override
		void clockLo(Video v) {
			// release ba for sprite 6
			v.releaseStun(StunSource.SPRITE6);
			// fetch sprite pointer
			spfetch(v, 7);
		}

		@Override
		void clockHi(Video v) {
			// If enabled, steal a cpu cycle for first byte fetch.
			sdfetch1(v, 7);
		}
	},
	S7S {
		@Override
		void clockLo(Video v) {
			// If enabled, fetch second byte, else idle
			sdfetch2(v, 7);
		}

		@Override
		void clockHi(Video v) {
			// If enabled, steal a cpu cycle for third byte fetch.
			sdfetch3(v, 7);
		}
	},
	R0 {
		@Override
		void clockLo(Video v) {
			// release ba for sprite 7
			v.releaseStun(StunSource.SPRITE7);
			refresh(v);
		}
		@Override
		void clockHi(Video v) {
			// Nothing
		}
	},
	R1 {
		@Override
		void clockLo(Video v) {
			refresh(v);
			// TODO check if badline coming- if so, negate BA
		}

		@Override
		void clockHi(Video v) {
			// Nothing
		}
	},
	R2 {
		@Override
		void clockLo(Video v) {
			refresh(v);
		}

		@Override
		void clockHi(Video v) {
			// Nothing
		}
	},
	R3 {
		@Override
		void clockLo(Video v) {
			refresh(v);
		}

		@Override
		void clockHi(Video v) {
			// Nothing
		}
	},
	R4 {
		@Override
		void clockLo(Video v) {
			refresh(v);
			// increment sprite counters- "cycle 15" according to vic656x.txt
			for (Sprite s : v.sprites) {
				if (s.notAgain) {
					s.mcbase += 2;
				}
			}
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 0);
		}
	},
	G00 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
			// increment sprite counters continued- "cycle 16"
			for (Sprite s : v.sprites) {
				if (s.notAgain) {
					s.mcbase++;
					if (s.mcbase == 63) {	// reached the end of sprite data
						s.dma = false;
						s.display = false;
					}
				}
			}		
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 1);
		}
	},
	G01 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 2);
		}
	},
	G02 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 3);
		}
	},
	G03 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 4);
		}
	},
	G04 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 5);
		}
	},
	G05 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 6);
		}
	},
	G06 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 7);
		}
	},
	G07 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 8);
		}
	},
	G08 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 9);
		}
	},
	G09 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 10);
		}
	},
	G10 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 11);
		}
	},
	G11 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 12);
		}
	},
	G12 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 13);
		}
	},
	G13 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 14);
		}
	},
	G14 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 15);
		}
	},
	G15 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 16);
		}
	},
	G16 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 17);
		}
	},
	G17 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 18);
		}
	},
	G18 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 19);
		}
	},
	G19 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 20);
		}
	},
	G20 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 21);
		}
	},
	G21 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 22);
		}
	},
	G22 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 23);
		}
	},
	G23 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 24);
		}
	},
	G24 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 25);
		}
	},
	G25 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 26);
		}
	},
	G26 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 27);
		}
	},
	G27 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 28);
		}
	},
	G28 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 29);
		}
	},
	G29 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 30);
		}
	},
	G30 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 31);
		}
	},
	G31 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 32);
		}
	},
	G32 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 33);
		}
	},
	G33 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 34);
		}
	},
	G34 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 35);
		}
	},
	G35 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 36);
		}
	},
	G36 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 37);
		}
	},
	G37 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 38);
		}
	},
	G38 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
		}

		@Override
		void clockHi(Video v) {
			// if badline, steal cycle for c fetch
			cFetch(v, 39);
		}
	},
	G39 {
		@Override
		void clockLo(Video v) {
			// TODO g fetch
			
			if (v.variantProperty.get() == Variant.PAL_NEW || v.variantProperty.get() == Variant.PAL_OLD) {
				// Check if Sprite 0 enabled, if so negate BA
				if (v.sprites[0].enabled) v.prepareStun(StunSource.SPRITE0);
				cycle55(v);
			}
		}

		@Override
		void clockHi(Video v) {
			// Nothing
		}
	},
	I0 {
		@Override
		void clockLo(Video v) {
			// TODO Idle fetch
			
			if (v.variantProperty.get() == Variant.NTSC_OLD) {
				// Check if Sprite 0 enabled, if so negate BA
				if (v.sprites[0].enabled) v.prepareStun(StunSource.SPRITE0);
				cycle55(v);
			} else if (v.variantProperty.get() == Variant.PAL_NEW || v.variantProperty.get() == Variant.PAL_OLD) {
				cycle56(v);
			}
		}

		@Override
		void clockHi(Video v) {
			// Nothing
		}
	},
	I1 {
		@Override
		void clockLo(Video v) {
			// TODO Idle fetch
			
			switch (v.variantProperty.get()) {
			case NTSC_NEW:
				// Check if Sprite 0 enabled, if so negate BA
				if (v.sprites[0].enabled) v.prepareStun(StunSource.SPRITE0);
				cycle55(v);
				break;
			case NTSC_OLD:
				cycle56(v);
				break;
			default:	// PAL either old or new
				// Check if Sprite 1 enabled, if so negate BA
				if (v.sprites[1].enabled) v.prepareStun(StunSource.SPRITE1);
			}
		}

		@Override
		void clockHi(Video v) {
			// Nothing
		}
	},
	I2 {	// 6569 does not include this
		@Override
		void clockLo(Video v) {
			// TODO Idle fetch
			
			if (v.variantProperty.get() == Variant.NTSC_OLD) {
				// Check if Sprite 1 enabled, if so negate BA
				if (v.sprites[1].enabled) v.prepareStun(StunSource.SPRITE1);
			} else if (v.variantProperty.get() == Variant.NTSC_NEW) {
				cycle56(v);
			}
		}

		@Override
		void clockHi(Video v) {
			// Nothing
		}
	},
	I3 {	// 6569, 6567R56A do not include this
		@Override
		void clockLo(Video v) {
			// TODO Idle fetch
			// I3: if 6567R8 & Sprite 1 enabled, negate BA
			if (v.variantProperty.get() == Variant.NTSC_NEW) {
				// Check if Sprite 1 enabled, if so negate BA
				if (v.sprites[1].enabled) v.prepareStun(StunSource.SPRITE1);
			}
		}

		@Override
		void clockHi(Video v) {
			// Nothing
		}
	},
	;
	/**
	 * Inversion of control- most of state is kept in Video class, what changes is the action needing to be done.
	 * @param v a video instance to modify
	 */
	abstract void clockLo(Video v);
	abstract void clockHi(Video v);
	
	CycleType nextFor(Variant v) {	// gets next cycle- each variant has a different last cycle.
		return (ordinal() >= v.endOfLine) ? values()[0] : values()[ordinal() + 1];
	}
	/**
	 * Helper function to read from video and optionally mask color bits
	 */
	private static int vread(Video v, int addr, boolean withColor) {
		return v.memoryProperty.get().vread(addr) & (withColor ? 0xfff : 0xff);
	}
	private static void spfetch(Video v, int sprite) {
		v.sprites[sprite].pointer = vread(v, v.vmbase | 0x3f8 | sprite, false) << 6;
	}
	private static void sdfetch1(Video v, int sprite) {
		var s = v.sprites[sprite];
		if (s.dma) {
			// if ba was not negated in time, fetch does not get to set address, but still reads data... TODO?
			s.sequencer = !v.aecProperty.get() ? vread(v, s.pointer | s.mcount, false) << 16 : 0xff0000;
			s.mcount++;	// increments whether the fetch happens or not
		}
	}
	private static void sdfetch2(Video v, int sprite) {
		var s = v.sprites[sprite];
		if (s.dma) {
			s.sequencer |= vread(v, (s.pointer | s.mcount++), false) << 8;
		} else {
			v.memoryProperty.get().vread(0x3fff);	// idle
		}
	}
	private static void sdfetch3(Video v, int sprite) {
		var s = v.sprites[sprite];
		if (s.dma) {
			// if ba was not negated in time, fetch does not get to set address, but still reads data... TODO?
			s.sequencer = !v.aecProperty.get() ? vread(v, s.pointer | s.mcount, false) : 0xff;
			s.mcount++;	// increments whether the fetch happens or not
		}
	}
	private static void refresh(Video v) {
		vread(v, 0x3f00 | v.refreshCounter--, false);	// discard the result
	}
	private static void cFetch(Video v, int index) {	// performs a character fetch, stores in buffer
		if (!v.aecProperty.get() && v.bad) {
			v.lineBuffer[index] = (short) vread(v, v.vmbase, true);	// TODO more to address calculation
		}
	}
	private static void cycle55(Video v) {	// "cycle 55" according to vic656x.txt- does a bunch of sprite preparations
		for (Sprite s : v.sprites) {
			if (s.expandY) s.notAgain = !s.notAgain;
			s.firstLine = s.enabled && s.y == v.rasterCurrent;
		}
	}
	private static void cycle56(Video v) {	// continuation of above
		for (Sprite s : v.sprites) {
			if (s.firstLine && !s.dma) {
				s.dma = true;
				s.mcbase = 0;
				if (s.expandY) s.notAgain = false;
			}
		}
	}
}
