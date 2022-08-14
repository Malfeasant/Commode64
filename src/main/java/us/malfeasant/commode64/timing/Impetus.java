package us.malfeasant.commode64.timing;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.Preferences;

import javafx.animation.AnimationTimer;
import javafx.beans.property.SimpleObjectProperty;

/**
 * What makes everything go.  In order to get an exact fractional rate of ticks per second with only integer
 * math, we count up by number of cycles * elapsed time in ns, then count down by number of seconds * 1M- this
 * works because we're in effect dividing the rate by the number of intervals per second, which is the same as
 * multiplying by the reciprocal of the number of intervals per second, which is the period though it's off by
 * a factor of 1M since the target rate is in seconds and the period is in ns- which is where the * 1000000000
 * comes from.  simple!  (not really, it took running a few iterations by hand to fully grok why it works)
 * To be really pedantic, the powerline cycles would have to be inserted into their proper place in the train
 * of crystal cycles, but that would be a little ridiculous.  This is kind of excessive already.
 * @author Malfeasant
 */
public class Impetus extends AnimationTimer {
	private long then;	// used to calculate how much time has elapsed since last run
	
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	
	private final SimpleObjectProperty<Crystal> crystalProp;
	private final SimpleObjectProperty<Power> powerProp;
	
	private final List<CrystalListener> crystalListeners = new CopyOnWriteArrayList<>();	// these will be used by both
	private final List<PowerListener> powerListeners = new CopyOnWriteArrayList<>();	// fx thread and bg thread
	
	public Impetus() {
		var crystal = Crystal.valueOf(prefs.get(Crystal.class.getSimpleName(), Crystal.NTSC.name()));
		var power = Power.valueOf(prefs.get(Power.class.getSimpleName(), Power.NA.name()));
		
		crystalProp = new SimpleObjectProperty<>(crystal);
		powerProp = new SimpleObjectProperty<>(power);
	}
	
	private void powerTick() {
		for (var l : powerListeners) {
			l.powerTick();
		}
	}
	private void crystalTick(int howmany) {
		for (var l : crystalListeners) {
			l.crystalTick(howmany);
		}
	}
	
	public void addCrystalListener(CrystalListener r) {
		if (r == null) throw new NullPointerException("Listener must not be null!");	// shouldn't happen, throw early
		crystalListeners.add(r);
	}
	public void addPowerListener(PowerListener r) {
		if (r == null) throw new NullPointerException("Listener must not be null!");	// shouldn't happen, throw early
		powerListeners.add(r);
	}
	public void removeCrystalListener(CrystalListener r) {
		crystalListeners.remove(r);
	}
	public void removePowerListener(PowerListener r) {
		powerListeners.remove(r);
	}
	
	private long powerTicks;
	private long crystalTicks;
	
	@Override
	public void handle(long now) {
		if (then != 0) {	// first run is special
			var elapsed = now - then;
			powerTicks += powerProp.get().cycles * elapsed;
			while (powerTicks >= powerProp.get().seconds * 1000000000) {
				powerTicks -= powerProp.get().seconds * 1000000000;
				powerTick();
			}
			crystalTicks += crystalProp.get().cycles * elapsed;
			var howmany = crystalTicks / (crystalProp.get().seconds * 1000000000L);
			crystalTicks = crystalTicks % (crystalProp.get().seconds * 1000000000L);
			assert (howmany < Integer.MAX_VALUE) : "Something went wrong...";
			crystalTick((int) howmany);
		}
		then = now;
	}
}
