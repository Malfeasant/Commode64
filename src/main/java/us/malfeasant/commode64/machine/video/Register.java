package us.malfeasant.commode64.machine.video;

public enum Register {
	D000 {	// 53248: Sprite 0 Horizontal Position (lsB)
		@Override
		int peek(Video v) {
			return getSpriteX(v, 0);
		}
		@Override
		void poke(Video v, int data) {
			setSpriteX(v, 0, data);
		}
	},
	D001 {	// 53249: Sprite 0 Vertical Position
		@Override
		int peek(Video v) {
			return v.sprites[0].y;
		}
		@Override
		void poke(Video v, int data) {
			v.sprites[0].y = data;
		}
	},
	D002 {	// 53250: Sprite 1 Horizontal Position (lsB)
		@Override
		int peek(Video v) {
			return getSpriteX(v, 1);
		}
		@Override
		void poke(Video v, int data) {
			setSpriteX(v, 1, data);
		}
	},
	D003 {	// 53251: Sprite 1 Vertical Position
		@Override
		int peek(Video v) {
			return v.sprites[1].y;
		}
		@Override
		void poke(Video v, int data) {
			v.sprites[1].y = data;
		}
	},
	D004 {	// 53252: Sprite 2 Horizontal Position (lsB)
		@Override
		int peek(Video v) {
			return getSpriteX(v, 2);
		}
		@Override
		void poke(Video v, int data) {
			setSpriteX(v, 2, data);
		}
	},
	D005 {	// 53253: Sprite 2 Vertical Position
		@Override
		int peek(Video v) {
			return v.sprites[2].y;
		}
		@Override
		void poke(Video v, int data) {
			v.sprites[2].y = data;
		}
	},
	D006 {	// 53254: Sprite 3 Horizontal Position (lsB)
		@Override
		int peek(Video v) {
			return getSpriteX(v, 3);
		}
		@Override
		void poke(Video v, int data) {
			setSpriteX(v, 3, data);
		}
	},
	D007 {	// 53255: Sprite 3 Vertical Position
		@Override
		int peek(Video v) {
			return v.sprites[3].y;
		}
		@Override
		void poke(Video v, int data) {
			v.sprites[3].y = data;
		}
	},
	D008 {	// 53256: Sprite 4 Horizontal Position (lsB)
		@Override
		int peek(Video v) {
			return getSpriteX(v, 4);
		}
		@Override
		void poke(Video v, int data) {
			setSpriteX(v, 4, data);
		}
	},
	D009 {	// 53257: Sprite 4 Vertical Position
		@Override
		int peek(Video v) {
			return v.sprites[4].y;
		}
		@Override
		void poke(Video v, int data) {
			v.sprites[4].y = data;
		}
	},
	D00A {	// 53258: Sprite 5 Horizontal Position (lsB)
		@Override
		int peek(Video v) {
			return getSpriteX(v, 5);
		}
		@Override
		void poke(Video v, int data) {
			setSpriteX(v, 5, data);
		}
	},
	D00B {	// 53259: Sprite 5 Vertical Position
		@Override
		int peek(Video v) {
			return v.sprites[5].y;
		}
		@Override
		void poke(Video v, int data) {
			v.sprites[5].y = data;
		}
	},
	D00C {	// 53260: Sprite 6 Horizontal Position (lsB)
		@Override
		int peek(Video v) {
			return getSpriteX(v, 6);
		}
		@Override
		void poke(Video v, int data) {
			setSpriteX(v, 6, data);
		}
	},
	D00D {	// 53261: Sprite 6 Vertical Position
		@Override
		int peek(Video v) {
			return v.sprites[6].y;
		}
		@Override
		void poke(Video v, int data) {
			v.sprites[6].y = data;
		}
	},
	D00E {	// 53262: Sprite 7 Horizontal Position (lsB)
		@Override
		int peek(Video v) {
			return getSpriteX(v, 7);
		}
		@Override
		void poke(Video v, int data) {
			setSpriteX(v, 7, data);
		}
	},
	D00F {	// 53263: Sprite 7 Vertical Position
		@Override
		int peek(Video v) {
			return v.sprites[7].y;
		}
		@Override
		void poke(Video v, int data) {
			v.sprites[7].y = data;
		}
	},
	D010 {	// 53264: Sprite Horizontal Position MSbs
		@Override
		int peek(Video v) {
			int data = 0;
			for (Sprite s : v.sprites) {
				data = s.x & 0x100 | data;	// if high bit of position set, move it to temp position
				data >>= 1;	// then shift right by one- so first ends up in bit 0, last in bit 7, as expected
			}
			return data;
		}
		@Override
		void poke(Video v, int data) {
			for (Sprite s : v.sprites) {
				s.x = (data & 1) != 0 ? s.x | 0x100 : s.x & 0xff;	// test lsb of data, set msb of x accordingly
				data >>= 1;	// shift right one- so 0 bit is used for sprite 0, 7 bit for sprite 7, as expected
			}
		}
	},
	D011 {	// 53265: Vertical Fine Scrolling and Control Register
		@Override
		int peek(Video v) {
			int data = (v.rasterCurrent & 0x100) >> 1;	// start with high bit of raster
			if (v.ecm) data |= 0x40;
			if (v.bmm) data |= 0x20;
			if (v.den) data |= 0x10;
			if (v.rsel) data |= 8;
			data |= v.yscroll & 7;	// shouldn't ever go higher than 7, but just in case
			return data;
		}
		@Override
		void poke(Video v, int data) {
			v.yscroll = data & 7;
			if ((data & 0x80) != 0) {	// test bit 7, 
				v.rasterCompare |= 0x100;	// if 1, set bit 8 of rc
			} else {
				v.rasterCompare &= 0xff;	// otherwise, clear bit 8 of rc
			}
			v.ecm = (data & 0x40) != 0;
			v.bmm = (data & 0x20) != 0;
			v.den = (data & 0x10) != 0;
			v.rsel = (data & 8) != 0;
		}
	},
	D012 {	// 53266: Raster (read current, write compare)
		@Override
		int peek(Video v) {
			return v.rasterCurrent & 0xff;	// raster is 9 bits, mask off the extra, it's in D011
		}
		@Override
		void poke(Video v, int data) {
			v.rasterCompare = (v.rasterCompare & 0x100) | data;
		}
	},
	D013 {	// 53267: Light pen horizontal position
		@Override
		int peek(Video v) {
			// TODO Not sure how I'm going to implement this... would be trivial to use mouse pointer position for example
			return 0;
		}
		@Override
		void poke(Video v, int data) {
			// nothing- write has no effect
		}
	},
	D014 {	// 53268: Light pen vertical position
		@Override
		int peek(Video v) {
			// TODO Not sure how I'm going to implement this... would be trivial to use mouse pointer position for example
			return 0;
		}
		@Override
		void poke(Video v, int data) {
			// nothing- write has no effect
		}
	},
	D015 {	// 53269: Sprite Enable
		@Override
		int peek(Video v) {
			int data = 0;
			for (Sprite s : v.sprites) {
				if (s.enabled) data |= 0x100;
				data >>= 1;	// shift right so first ends up in bit 0
			}
			return data;
		}
		@Override
		void poke(Video v, int data) {
			for (Sprite s : v.sprites) {
				s.enabled = (data & 1) != 0;	// check rightmost bit
				data >>= 1;	// shift right to be ready for the next
			}
		}
	},
	D016 {	// 53270: Horizontal fine scrolling & control
		@Override
		int peek(Video v) {
			int data = 0xc0;	// top bits are unconnected, always read 1s
			if (v.res) data |= 0x20;
			if (v.mcm) data |= 0x10;
			if (v.csel) data |= 8;
			data |= v.xscroll;
			return data;
		}
		@Override
		void poke(Video v, int data) {
			v.res = (data & 0x20) != 0;
			v.mcm = (data & 0x10) != 0;
			v.csel = (data & 8) != 0;
			v.xscroll = data & 7;
		}
	},
	D017 {	// 53271: Sprite y expansion
		@Override
		int peek(Video v) {
			int data = 0;
			for (Sprite s : v.sprites) {
				if (s.expandY) data |= 0x100;
				data >>= 1;	// shift right so first ends up in bit 0
			}
			return data;
		}
		@Override
		void poke(Video v, int data) {
			for (Sprite s : v.sprites) {
				s.expandY = (data & 1) != 0;	// check rightmost bit
				data >>= 1;	// shift right to be ready for the next
			}
		}
	},
	D018 {	// 53272: memory control reg
		@Override
		int peek(Video v) {
			int data = 1;	// bit 0 is unconnected, always reads 1
			data |= (v.vmbase >> 6);
			data |= (v.chbase >> 10);
			return data;
		}
		@Override
		void poke(Video v, int data) {
			v.vmbase = (data & 0xf0 << 6);	// bits 7-4 move to bits 13-10
			v.chbase = (data & 0xe << 10);	// bits 3-1 move to bits 13-11
		}
	},
	D019 {	// 53273: Interrupt register
		@Override
		int peek(Video v) {
			int data = 0x70;	// unused bits read 1
			// TODO return status of interrupt bits
			return data;
		}
		@Override
		void poke(Video v, int data) {
			// TODO clear interrupts according to set bits
		}
	},
	D01A {	// 53274: Interrupt mask register
		@Override
		int peek(Video v) {
			int data = 0xf0;	// unused bits
			// TODO get interrupt mask
			return data;
		}
		@Override
		void poke(Video v, int data) {
			// TODO set interrupt mask
		}
	},
	D01B {	// 53275: Sprite/data priority
		@Override
		int peek(Video v) {
			int data = 0;
			for (Sprite s : v.sprites) {
				if (s.fgPriority) data |= 0x100;
				data >>= 1;	// shift right so first ends up in bit 0
			}
			return data;
		}
		@Override
		void poke(Video v, int data) {
			for (Sprite s : v.sprites) {
				s.fgPriority = (data & 1) != 0;	// check rightmost bit
				data >>= 1;	// shift right to be ready for the next
			}
		}
	},
	D01C {	// 53276: Enable sprite multicolor
		@Override
		int peek(Video v) {
			int data = 0;
			for (Sprite s : v.sprites) {
				if (s.multicolor) data |= 0x100;
				data >>= 1;	// shift right so first ends up in bit 0
			}
			return data;
		}
		@Override
		void poke(Video v, int data) {
			for (Sprite s : v.sprites) {
				s.multicolor = (data & 1) != 0;	// check rightmost bit
				data >>= 1;	// shift right to be ready for the next
			}
		}
	},
	D01D {	// 53277: Sprite horizontal expansion
		@Override
		int peek(Video v) {
			int data = 0;
			for (Sprite s : v.sprites) {
				if (s.expandX) data |= 0x100;
				data >>= 1;	// shift right so first ends up in bit 0
			}
			return data;
		}
		@Override
		void poke(Video v, int data) {
			for (Sprite s : v.sprites) {
				s.expandX = (data & 1) != 0;	// check rightmost bit
				data >>= 1;	// shift right to be ready for the next
			}
		}
	},
	D01E {	// 53278: Sprite-sprite collision
		@Override
		int peek(Video v) {
			int data = 0;
			for (Sprite s : v.sprites) {
				if (s.collidedS) data |= 0x100;
				s.collidedS = false;	// read clears the flag
				data >>= 1;	// shift right so first ends up in bit 0
			}
			return data;
		}
		@Override
		void poke(Video v, int data) {
			// Nothing- writes have no effect
		}
	},
	D01F {	// 53279: Sprite-video collision
		@Override
		int peek(Video v) {
			int data = 0;
			for (Sprite s : v.sprites) {
				if (s.collidedV) data |= 0x100;
				s.collidedV = false;	// read clears the flag
				data >>= 1;	// shift right so first ends up in bit 0
			}
			return data;
		}
		@Override
		void poke(Video v, int data) {
			// Nothing- writes have no effect
		}
	},
	D020 {
		@Override
		int peek(Video v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		void poke(Video v, int data) {
			// TODO Auto-generated method stub
			
		}
	}, D021 {
		@Override
		int peek(Video v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		void poke(Video v, int data) {
			// TODO Auto-generated method stub
			
		}
	}, D022 {
		@Override
		int peek(Video v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		void poke(Video v, int data) {
			// TODO Auto-generated method stub
			
		}
	}, D023 {
		@Override
		int peek(Video v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		void poke(Video v, int data) {
			// TODO Auto-generated method stub
			
		}
	}, D024 {
		@Override
		int peek(Video v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		void poke(Video v, int data) {
			// TODO Auto-generated method stub
			
		}
	}, D025 {
		@Override
		int peek(Video v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		void poke(Video v, int data) {
			// TODO Auto-generated method stub
			
		}
	}, D026 {
		@Override
		int peek(Video v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		void poke(Video v, int data) {
			// TODO Auto-generated method stub
			
		}
	}, D027 {
		@Override
		int peek(Video v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		void poke(Video v, int data) {
			// TODO Auto-generated method stub
			
		}
	},
	D028 {
		@Override
		int peek(Video v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		void poke(Video v, int data) {
			// TODO Auto-generated method stub
			
		}
	}, D029 {
		@Override
		int peek(Video v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		void poke(Video v, int data) {
			// TODO Auto-generated method stub
			
		}
	}, D02A {
		@Override
		int peek(Video v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		void poke(Video v, int data) {
			// TODO Auto-generated method stub
			
		}
	}, D02B {
		@Override
		int peek(Video v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		void poke(Video v, int data) {
			// TODO Auto-generated method stub
			
		}
	}, D02C {
		@Override
		int peek(Video v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		void poke(Video v, int data) {
			// TODO Auto-generated method stub
			
		}
	}, D02D {
		@Override
		int peek(Video v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		void poke(Video v, int data) {
			// TODO Auto-generated method stub
			
		}
	}, D02E {
		@Override
		int peek(Video v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		void poke(Video v, int data) {
			// TODO Auto-generated method stub
			
		}
	}, D02F {
		@Override
		int peek(Video v) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		void poke(Video v, int data) {
			// TODO Auto-generated method stub
			
		}
	},
	;
	abstract int peek(Video v);
	abstract void poke(Video v, int data);
	private static int getSpriteX(Video v, int s) {
		return v.sprites[s].x & 0xff;
	}
	private static void setSpriteX(Video v, int s, int x) {
		v.sprites[s].x &= 0x100;	// clear low bits
		v.sprites[s].x |= x & 0xff;
	}
}
