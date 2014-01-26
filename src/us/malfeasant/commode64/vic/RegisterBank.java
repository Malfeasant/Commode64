package us.malfeasant.commode64.vic;

class RegisterBank {
	private final SpriteControl[] spriteControl = new SpriteControl[8];
	RegisterBank() {
		for (int i = 0; i < spriteControl.length; i++) {
			spriteControl[i] = new SpriteControl();
		}
	}
	int access(int index, int data, boolean wr) {
		if (index >= Reg.values().length) return -1;
		return Reg.values()[index].access(this, data, wr);
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
	private enum Reg {
		SP0X {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				return env.spriteLoX(ordinal() >> 1, data, wr);
			}
		}, SP0Y {
			@Override
			protected int access(RegisterBank env, int data, boolean wr) {
				// TODO Auto-generated method stub
				return 0;
			}
		},
		SP1X, SP1Y,
		SP2X, SP2Y,
		SP3X, SP3Y,
		SP4X, SP4Y,
		SP5X, SP5Y,
		SP6X, SP6Y,
		SP7X, SP7Y,
		MSIGX, SCROLY, RASTER, 
		LPENX, LPENY, 
		SPENA,
		SCROLX, YXPAND,
		VMCSB,
		VICIRQ, IRQMASK,
		SPBGPR, SPMC, XXPAND, SPSPCL, SPBGCL,
		EXTCOL, BGCOL0, BGCOL1, BGCOL2, BGCOL3,
		SPMC0, SPMC1,
		SP0COL, SP1COL, SP2COL, SP3COL, SP4COL, SP5COL, SP6COL, SP7COL;
		protected abstract int access(RegisterBank env, int data, boolean wr);
	}
}
