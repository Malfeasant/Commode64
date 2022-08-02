package us.malfeasant.commode64;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javafx.beans.property.SimpleObjectProperty;
import us.malfeasant.commode64.timing.Crystal;
import us.malfeasant.commode64.timing.CycleListener;
import us.malfeasant.commode64.timing.Power;

/**
 * What makes everything go.  Manages a background thread that runs the emulation.  Both power and crystal
 * run on the same thread, but on different schedules.  Neither have to be perfect as long as long term average
 * is correct, but jitter in Crystal would probably be more noticeable, so we will go to great lengths to avoid it.
 * @author Malfeasant
 */
public class Impetus {
	private static final long PERIOD = 5;	// more frequent than screen refreshes
	private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
	
	private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
	
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	
	private final SimpleObjectProperty<Crystal> crystalProp;
	private final SimpleObjectProperty<Power> powerProp;
	
	private final List<CycleListener> crystalListeners = new ArrayList<>();
	private final List<CycleListener> powerListeners = new ArrayList<>();
	
	public Impetus() {
		var crystal = Crystal.valueOf(prefs.get(Crystal.class.getSimpleName(), Crystal.NTSC.name()));
		var power = Power.valueOf(prefs.get(Power.class.getSimpleName(), Power.US.name()));
		
		crystalProp = new SimpleObjectProperty<>(crystal);
		powerProp = new SimpleObjectProperty<>(power);
		
		exec.scheduleAtFixedRate(() -> crystalTick(), 0, PERIOD, TIME_UNIT);
		exec.scheduleAtFixedRate(() -> powerTick(), 0, 100, TIME_UNIT);
		
		
	}
	
	private void powerTick() {
		for (var l : powerListeners) {
			l.fire(powerProp.get().cycles);
		}
	}
	private void crystalTick() {
		
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
		System.out.println("Long term average: " + tickrate + " ticks per second.  Thread " + Thread.currentThread());
		tick = 0;
		lastTime = now;
	}
	
	public void shutdown() {
		exec.shutdownNow();
	}
}
