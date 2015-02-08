package us.malfeasant.commode64.cia;

import java.util.EventListener;

public interface ChangeListener extends EventListener {
	void updateTime(int h, int m, int s, int t);
	void updateLatch(int h, int m, int s, int t);
	void updateAlarm(int h, int m, int s, int t);
}
