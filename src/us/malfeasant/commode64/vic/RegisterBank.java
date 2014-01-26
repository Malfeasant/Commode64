package us.malfeasant.commode64.vic;

class RegisterBank {
	private static final int SPRITES = 8;
	
	// 00 - 10
	// sprite x & y managed by SpriteControl[]
	private final SpriteControl[] spriteControl = new SpriteControl[SPRITES];
	// 11
	private int fineY;
	private boolean row25;
	private boolean dEn;
	private boolean bmEn;
	private boolean extEn;
	// 11 & 12
	private int rasterComp;	// write causes interrupt when raster reaches this point
	private int raster;	// reads current line
	// 13
	private int lpX;
	// 14
	private int lpY;
	// 15
	// sprite enable managed by SpriteControl[]
	// 16
	private int fineX;
	private boolean col40;
	private boolean mcEn;
	private boolean reset;
	// 17
	// sprite vert expand managed by SpriteControl[]
	// 18
	private int charBase;
	private int vmBase;
	// 19
	private boolean irqRaster;
	private boolean irqSpFg;
	private boolean irqSpSp;
	private boolean irqLP;
	private boolean irqAny;
	// 1a
	private boolean enRaster;
	private boolean enSpFg;
	private boolean enSpSp;
	private boolean enLp;
	
	RegisterBank() {
		for (int i = 0; i < spriteControl.length; i++) {
			spriteControl[i] = new SpriteControl();
		}
	}
	int access(int addr, int data, boolean wr) {
		return (addr < Reg.values().length) ? Reg.values()[addr].access(this, data, wr) : -1;
	}
	private enum Reg {
		SP0X, SP0Y, SP1X, SP1Y, SP2X, SP2Y, SP3X, SP3Y,
		SP4X, SP4Y, SP5X, SP5Y, SP6X, SP6Y, SP7X, SP7Y,
		MSIGX {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				int weight = 1;
				for (SpriteControl sc : env.spriteControl) {
					if (wr) {
						if ((data & weight) == 0)
							sc.x &= 0xff;
						else
							sc.x |= 0x100;
					} else {
						if ((sc.x & 0x100) == 0)
							data &= ~weight;
						else
							data |= weight;
					}
					weight <<= 1;
				}
				return data;
			}
		}, SCROLY {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (wr) {
					env.fineY = data & 7;
					env.row25 = (data & 8) != 0;
					env.dEn = (data & 0x10) != 0;
					env.bmEn = (data & 0x20) != 0;
					env.extEn = (data & 0x40) != 0;
					if ((data & 0x80) == 0)
						env.rasterComp &= 0xff;
					else
						env.rasterComp |= 0x100;
				} else {
					data = env.fineY;
					if (env.row25) data |= 8;
					if (env.dEn) data |= 0x10;
					if (env.bmEn) data |= 0x20;
					if (env.extEn) data |= 0x40;
					if (env.raster > 0x100) data |= 0x80; 
				}
				return data;
			}
		}, RASTER {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (wr) {
					env.rasterComp = (env.rasterComp & 0x100) | data;
				} else {
					data = env.raster & 0xff;
				}
				return data;
			}
		}, LPENX {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (!wr) {
					data = env.lpX >> 1;
				}
				return data;
			}
		}, LPENY {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (!wr) {
					data = env.lpY;
				}
				return data;
			}
		}, SPENA {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				int weight = 1;
				for (SpriteControl sc : env.spriteControl) {
					if (wr) {
						sc.enable = (data & weight) != 0;
					} else {
						if (sc.enable)
							data |= weight;
						else
							data &= ~weight;
					}
					weight <<= 1;
				}
				return data;
			}
		}, SCROLX {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (wr) {
					env.fineX = data & 7;
					env.col40 = (data & 8) != 0;
					env.mcEn = (data & 0x10) != 0;
					env.reset = (data & 0x20) != 0;
				} else {
					data = env.fineX | 0xc0;	// high 2 bits not connected
					if (env.col40) data |= 8;
					if (env.mcEn) data |= 0x10;
					if (env.reset) data |= 0x20;
				}
				return data;
			}
		}, YXPAND {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				int weight = 1;
				for (SpriteControl sc : env.spriteControl) {
					if (wr) {
						sc.expandY = (data & weight) != 0;
					} else {
						if (sc.expandY)
							data |= weight;
						else
							data &= ~weight;
					}
					weight <<= 1;
				}
				return data;
			}
		}, VMCSB {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (wr) {	// TODO - maybe prefer to store as expanded address?
					env.charBase = (data & 0xe) >> 1;
					env.vmBase = (data & 0xf0) >> 4;
				} else {
					data = 1;	// unused bit
					data |= env.charBase << 1;
					data |= env.vmBase << 4;
				}
				return data;
			}
		}, VICIRQ {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (!wr) {
					data = 0x70;	// unused bits
					if (env.irqRaster) data |= 1;
					if (env.irqSpFg) data |= 2;
					if (env.irqSpSp) data |= 4;
					if (env.irqLP) data |= 8;
					if (env.irqAny) data |= 0x80;
				}
				return data;
			}
		}, IRQMASK {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (wr) {
					env.enRaster = (data & 1) != 0;
					env.enSpFg = (data & 2) != 0;
					env.enSpSp = (data & 4) != 0;
					env.enLp = (data & 8) != 0;
				}
				return data;
			}
		}, SPBGPR {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (wr) {
					
				} else {
					
				}
				return data;
			}
		}, SPMC {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (wr) {
					
				} else {
					
				}
				return data;
			}
		}, XXPAND {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (wr) {
					
				} else {
					
				}
				return data;
			}
		}, SPSPCL {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (wr) {
					
				} else {
					
				}
				return data;
			}
		}, SPBGCL {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (wr) {
					
				} else {
					
				}
				return data;
			}
		}, EXTCOL {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (wr) {
					
				} else {
					
				}
				return data;
			}
		}, BGCOL0 {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (wr) {
					
				} else {
					
				}
				return data;
			}
		}, BGCOL1 {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (wr) {
					
				} else {
					
				}
				return data;
			}
		}, BGCOL2 {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (wr) {
					
				} else {
					
				}
				return data;
			}
		}, BGCOL3 {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (wr) {
					
				} else {
					
				}
				return data;
			}
		}, SPMC0 {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (wr) {
					
				} else {
					
				}
				return data;
			}
		}, SPMC1 {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				if (wr) {
					
				} else {
					
				}
				return data;
			}
		}, SP0COL, SP1COL, SP2COL, SP3COL, SP4COL, SP5COL, SP6COL, SP7COL;
		protected int access(RegisterBank env, int data, boolean wr) {
			int addr = ordinal();
			if ((addr & 0xf0) == 0) {	// sprite positions
				SpriteControl sc = env.spriteControl[addr >> 1];
				if ((addr & 1) == 0) {	// X position
					data = spriteLoX(sc, data, wr);
				} else {	// Y
					data = spriteY(sc, data, wr);
				}
			} else if (addr > SP0COL.ordinal()) {	// sprite color
				data = spriteColor(env.spriteControl[addr - Reg.SP0COL.ordinal()], data, wr);
			}
			return data;
		}
		private int spriteLoX(SpriteControl sc, int data, boolean wr) {
			if (wr)
				sc.x = (sc.x & 0x100) | data;
			else
				data = sc.x;
			return data;
		}
		private int spriteY(SpriteControl sc, int data, boolean wr) {
			if (wr)
				sc.y = data;
			else
				data = sc.y;
			return data;
		}
		private int spriteColor(SpriteControl sc, int data, boolean wr) {
			if (wr)
				sc.color = data & 0xf;
			else
				data = sc.color | 0xf0;	// high bits unconnected, so read high
			return data;
		}
	}
}
