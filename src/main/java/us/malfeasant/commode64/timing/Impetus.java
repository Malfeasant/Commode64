package us.malfeasant.commode64.timing;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javafx.beans.property.SimpleObjectProperty;

/**
 * What makes everything go.  Manages a background thread that runs the emulation.  Both power and crystal
 * run on the same thread.  In order to get an exact fractional rate of ticks per second with only integer
 * math, we count up by number of cycles * interval in ms, then count down by number of seconds * 1000- this
 * works because we're in effect dividing the rate by the number of intervals per second, which is the same as
 * multiplying by the reciprocal of the number of intervals per second, which is the period though it's off by
 * a factor of 1000 since the target rate is in seconds and the period is in ms- which is where the * 1000
 * comes from.  simple!  (not really, it took running a few iterations by hand to fully grok why it works)
 * To be really pedantic, the powerline cycles would have to be inserted into their proper place in the train
 * of crystal cycles, but that would be a little ridiculous.  This is kind of excessive already.
 * @author Malfeasant
 */
public class Impetus {
	private static final long PERIOD = 8;	// more frequent than screen refreshes
	private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
	
	private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
	
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	
	private final SimpleObjectProperty<Crystal> crystalProp;
	private final SimpleObjectProperty<Power> powerProp;
	
	private final List<Runnable> crystalListeners = new CopyOnWriteArrayList<>();	// these will be used by both
	private final List<Runnable> powerListeners = new CopyOnWriteArrayList<>();	// fx thread and bg thread
	
	public Impetus() {
		var crystal = Crystal.valueOf(prefs.get(Crystal.class.getSimpleName(), Crystal.PAL.name()));
		var power = Power.valueOf(prefs.get(Power.class.getSimpleName(), Power.EU.name()));
		//var crystal = Crystal.valueOf(prefs.get(Crystal.class.getSimpleName(), Crystal.NTSC.name()));
		//var power = Power.valueOf(prefs.get(Power.class.getSimpleName(), Power.NA.name()));
		
		crystalProp = new SimpleObjectProperty<>(crystal);
		powerProp = new SimpleObjectProperty<>(power);
		
		exec.scheduleAtFixedRate(() -> tick(), 0, PERIOD, TIME_UNIT);
	}
	
	private void powerTick() {
		for (var l : powerListeners) {
			l.run();
		}
	}
	private void crystalTick() {
		for (var l : crystalListeners) {
			l.run();
		}
	}
	
	public void addCrystalListener(Runnable r) {
		if (r == null) throw new NullPointerException("Listener must not be null!");	// shouldn't happen, throw early
		crystalListeners.add(r);
	}
	public void addPowerListener(Runnable r) {
		if (r == null) throw new NullPointerException("Listener must not be null!");	// shouldn't happen, throw early
		powerListeners.add(r);
	}
	public void removeCrystalListener(Runnable r) {
		crystalListeners.remove(r);
	}
	public void removePowerListener(Runnable r) {
		powerListeners.remove(r);
	}
	
	private int powerTicks;
	private int crystalTicks;
	
	private void tick() {
		powerTicks += powerProp.get().cycles * PERIOD;
		while (powerTicks >= powerProp.get().seconds * 1000) {
			powerTicks -= powerProp.get().seconds * 1000;
			powerTick();
		}
		crystalTicks += crystalProp.get().cycles * PERIOD;
		while (crystalTicks >= crystalProp.get().seconds * 1000) {
			crystalTicks -= crystalProp.get().seconds * 1000;
			crystalTick();
		}
	}
	
	public void shutdown() {
		exec.shutdownNow();
	}
}
