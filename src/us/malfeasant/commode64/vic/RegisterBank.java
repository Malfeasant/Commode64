package us.malfeasant.commode64.vic;

import java.util.EnumMap;
import java.util.Map;

class RegisterBank {
	private static final int SPRITES = 8;
	
	// 00 - 10
	// sprite x & y managed by SpriteControl[]
	final SpriteControl[] spriteControl = new SpriteControl[SPRITES];
	// 11
	int fineY;
	boolean row25;
	boolean dEn;
	boolean bmEn;
	boolean extEn;
	// 11 & 12
	int rasterComp;	// write causes interrupt when raster reaches this point
	int raster;	// reads current line
	// 13
	int lpX;
	// 14
	int lpY;
	// 15
	// sprite enable managed by SpriteControl[]
	// 16
	int fineX;
	boolean col40;
	boolean mcEn;
	boolean reset;
	// 17
	// sprite vert expand managed by SpriteControl[]
	// 18
	int charBase;
	int vmBase;
	// 19
	boolean irqRaster;
	boolean irqSpFg;
	boolean irqSpSp;
	boolean irqLP;
	boolean irqAny;
	// 1a
	boolean enRaster;
	boolean enSpFg;
	boolean enSpSp;
	boolean enLp;
	// 1b
	// sprite to forground priority managed by SpriteControl[]
	// 1c
	// sprite multicolor enable managed by SpriteControl[]
	// 1d
	// sprite xexpand enable managed by SpriteControl[]
	// 1e
	// sprite-sprite collision managed by SpriteControl[]
	// 1f
	// sprite-forground collision managed by SpriteControl[]
	// 20 - 26
	final Map<Reg, Color> colors;
	// 27 - 2e
	// sprite colors managed by SpriteControl[]
	
	RegisterBank() {
		for (int i = 0; i < spriteControl.length; i++) {
			spriteControl[i] = new SpriteControl();
		}
		colors = new EnumMap<>(Reg.class);
		for (int i = 0x20; i < 0x27; i++) {
			colors.put(Reg.values()[i], Color.BLACK);
		}
	}
	int access(int addr, int data, boolean wr) {
		return (addr < Reg.values().length) ? Reg.values()[addr].access(this, data, wr) : -1;
	}
	private enum Reg {
		SP0X(Type.XPOS), SP0Y(Type.YPOS), SP1X(Type.XPOS), SP1Y(Type.YPOS),
		SP2X(Type.XPOS), SP2Y(Type.YPOS), SP3X(Type.XPOS), SP3Y(Type.YPOS),
		SP4X(Type.XPOS), SP4Y(Type.YPOS), SP5X(Type.XPOS), SP5Y(Type.YPOS),
		SP6X(Type.XPOS), SP6Y(Type.YPOS), SP7X(Type.XPOS), SP7Y(Type.YPOS),
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
				if (wr) {
					env.charBase = (data & 0xe) << 10;
					env.vmBase = (data & 0xf0) << 6;
				} else {
					data = 1;	// unused bit
					data |= env.charBase >> 10;
					data |= env.vmBase >> 6;
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
		}, SPFGPR {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				int weight = 1;
				for (SpriteControl sc : env.spriteControl) {
					if (wr) {
						sc.behind = (data & weight) != 0;
					} else {
						if (sc.behind)
							data |= weight;
						else
							data &= ~weight;
					}
					weight <<= 1;
				}
				return data;
			}
		}, SPMC {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				int weight = 1;
				for (SpriteControl sc : env.spriteControl) {
					if (wr) {
						sc.multicolor = (data & weight) != 0;
					} else {
						if (sc.multicolor)
							data |= weight;
						else
							data &= ~weight;
					}
					weight <<= 1;
				}
				return data;
			}
		}, XXPAND {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				int weight = 1;
				for (SpriteControl sc : env.spriteControl) {
					if (wr) {
						sc.expandX = (data & weight) != 0;
					} else {
						if (sc.expandX)
							data |= weight;
						else
							data &= ~weight;
					}
					weight <<= 1;
				}
				return data;
			}
		}, SPSPCL {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				int weight = 1;
				for (SpriteControl sc : env.spriteControl) {
					if (sc.collideS) {
						data |= weight;
						sc.collideS = false;
					} else
						data &= ~weight;
					weight <<= 1;
				}
				return data;
			}
		}, SPBGCL {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				int weight = 1;
				for (SpriteControl sc : env.spriteControl) {
					if (sc.collideFG) {
						data |= weight;
						sc.collideFG = false;
					} else
						data &= ~weight;
					weight <<= 1;
				}
				return data;
			}
		},
		EXTCOL(Type.COLOR), BGCOL0(Type.COLOR), BGCOL1(Type.COLOR), BGCOL2(Type.COLOR),
		BGCOL3(Type.COLOR), SPMC0(Type.COLOR), SPMC1(Type.COLOR),
		SP0COL(Type.SCOLOR), SP1COL(Type.SCOLOR), SP2COL(Type.SCOLOR), SP3COL(Type.SCOLOR),
		SP4COL(Type.SCOLOR), SP5COL(Type.SCOLOR), SP6COL(Type.SCOLOR), SP7COL(Type.SCOLOR);
		private enum Type {
			XPOS, YPOS, COLOR, SCOLOR, SPECIAL;
		}
		private final Type type;
		Reg(Type t) {
			type = t;
		}
		Reg() {
			this(Type.SPECIAL);
		}
		protected int access(RegisterBank env, int data, boolean wr) {
			int addr = ordinal();
			switch (type) {
			case XPOS:
				data = spriteLoX(env.spriteControl[addr >> 1], data, wr);
				break;
			case YPOS:
				data = spriteY(env.spriteControl[addr >> 1], data, wr);
				break;
			case COLOR:
				data = color(env, data, wr);
				break;
			case SCOLOR:
				data = spriteColor(env.spriteControl[addr - SP0COL.ordinal()], data, wr);
				break;
			case SPECIAL:
				// should not reach this, these override access()
				break;
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
				sc.color = Color.values()[data & 0xf];
			else
				data = sc.color.ordinal() | 0xf0;	// high bits unconnected, so read high
			return data;
		}
		private int color(RegisterBank env, int data, boolean wr) {
			if (wr)
				env.colors.put(this, Color.values()[data & 0xf]);
			else
				data = env.colors.get(this).ordinal() | 0xf0;
			return data;
		}
	}
}
