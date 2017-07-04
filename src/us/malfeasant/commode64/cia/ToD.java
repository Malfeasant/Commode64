package us.malfeasant.commode64.cia;

class ToD {
	private boolean setAlarm;
	
	void set(int addr, int data) {
	}
	int get(int addr) {
		return 0;
	}
	/**
	 * ticks the clock- assumed to happen once every tenth of a second
	 * @return true if time matches alarm
	 */
	boolean tick() {
		return false;
	}
}
