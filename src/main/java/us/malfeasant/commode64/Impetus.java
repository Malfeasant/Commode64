package us.malfeasant.commode64;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javafx.beans.binding.ObjectExpression;
import javafx.beans.property.SimpleObjectProperty;
import us.malfeasant.commode64.timing.Fraction;
import us.malfeasant.commode64.timing.IntervalListener;

/**
 * What makes everything go
 * @author Malfeasant
 */
public class Impetus {
	private static final long PERIOD = 5;
	private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
	
	private final SimpleObjectProperty<SpeedButtons.SpeedSet> speedProp = new SimpleObjectProperty<>();	// TODO expose prop
	
	private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
	private final ScheduledFuture<?> task;	// TODO what type for generic?
	
	private final Map<IntervalListener, DividerState> listeners = new IdentityHashMap<>();
	
	public Impetus() {
		task = exec.scheduleAtFixedRate(() -> tick(), 0, PERIOD, TIME_UNIT);
	}
	
	public void bindSpeedProp(ObjectExpression<SpeedButtons.SpeedSet> speed) {
		speedProp.bind(speed);
	}
	
	private int tick;
	private long lastTime = System.nanoTime();
	private double tickrate;
	
	private void tick() {
		tick++;
		if (tick < 200) return;
		var now = System.nanoTime();
		var nanos = now - lastTime;
		var secs = nanos / 1e9;
		var rate = tick / secs;
		tickrate *= 3;
		tickrate += rate;
		tickrate /= 4;
		System.out.print("Received " + tick + " ticks in " + secs + "s: " + rate + " ticks per second.\t");
		System.out.println("Long term average: " + tickrate + " ticks per second.");
		tick = 0;
		lastTime = now;
	}
	
	private class DividerState {
		private final Fraction frequency;
		private DividerState(Fraction f) {
			frequency = f;
		}
	}
	/**
	 * Adds a listener for periodic events- the listener will be called with a number of events each interval
	 * @param listener the listener to be notified each interval
	 * @param frequency a (fractional) number of events per second
	 */
	public void addListener(IntervalListener listener, Fraction frequency) {
		listeners.put(listener, new DividerState(frequency));
	}
	public void removeListener(IntervalListener listener) {
		listeners.remove(listener);
	}
	
	public void shutdown() {
		exec.shutdownNow();
	}
}
