package us.malfeasant.commode64.cpu;

enum State {
	RESET {
		@Override
		void clockHi(CPU c) {
			if (!c.resProp.get()) {
				// TODO complete reset process...
			}
		}
	},
	INTERRUPT {
		@Override
		void clockHi(CPU c) {
			// TODO Auto-generated method stub
			
		}
	},
	FETCH {
		@Override
		void clockHi(CPU c) {
			// TODO Auto-generated method stub
			
		}
	},
	WRITE {
		@Override
		void clockHi(CPU c) {
			// TODO Auto-generated method stub
			
		}
	},
	EXECUTE {
		@Override
		void clockHi(CPU c) {
			// TODO Auto-generated method stub
			
		}
	};
	
	abstract void clockHi(CPU c);
}
