package us.malfeasant.commode64;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javafx.beans.property.SimpleObjectProperty;
import us.malfeasant.commode64.timing.Crystal;
import us.malfeasant.commode64.timing.Power;

/**
 * What makes everything go.  Manages a background thread that runs the emulation.  Both power and crystal
 * run on the same thread, but on different schedules.  Neither have to be perfect as long as long term average
 * is correct, but jitter in Crystal would probably be more noticeable, so we will go to great lengths to avoid it.
 * @author Malfeasant
 */
public class Impetus {
	private static final long PERIOD = 8;	// more frequent than screen refreshes
	private static final TimeUnit TIME_UNIT = TimeUnit.MILLISECONDS;
	
	private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
	
	private final Preferences prefs = Preferences.userNodeForPackage(getClass());
	
	private final SimpleObjectProperty<Crystal> crystalProp;
	private final SimpleObjectProperty<Power> powerProp;
	
	private final List<Runnable> crystalListeners = new ArrayList<>();
	private final List<Runnable> powerListeners = new ArrayList<>();
	
	public Impetus() {
		var crystal = Crystal.valueOf(prefs.get(Crystal.class.getSimpleName(), Crystal.NTSC.name()));
		var power = Power.valueOf(prefs.get(Power.class.getSimpleName(), Power.NA.name()));
		
		crystalProp = new SimpleObjectProperty<>(crystal);
		powerProp = new SimpleObjectProperty<>(power);
		
		exec.scheduleAtFixedRate(() -> tick(), 0, PERIOD, TIME_UNIT);
		exec.scheduleAtFixedRate(() -> showRate(), 0, 1000, TIME_UNIT);
	}
	
	private void powerTick() {
		totalPower++;
		for (var l : powerListeners) {
			l.run();
		}
	}
	private void crystalTick() {
		totalCrystal++;
		for (var l : crystalListeners) {
			l.run();
		}
	}
	
	private void showRate() {	// this is all debug, confirming the speed is right- no need to keep it
		var now = System.nanoTime();
		var elapsed = (now - lastTime) * 1e-9;
		System.out.println("Elapsed time: " + elapsed + " seconds.");
		lastTime = now;
		var crate = totalCrystal / elapsed;
		var prate = totalPower / elapsed;
		crystalRate *= 99;
		crystalRate += crate;
		crystalRate /= 100;
		powerRate *= 99;
		powerRate += prate;
		powerRate /= 100;
		System.out.println("Crystal fired " + totalCrystal + " ticks for a rate of " + crystalRate + " ticks per second.");
		System.out.println("Power fired " + totalPower + " ticks for a rate of " + powerRate + " ticks per second.");
		totalCrystal = 0;
		totalPower = 0;
	}
	private int powerTicks;
	private int crystalTicks;
	
	private long totalPower;
	private long totalCrystal;
	private long lastTime = System.nanoTime();
	private double powerRate;
	private double crystalRate;
	
	private void tick() {
		powerTicks += powerProp.get().cycles * PERIOD;
		while (powerTicks >= powerProp.get().seconds * 1000) {
			powerTicks -= powerProp.get().seconds * 1000;
			powerTick();
			//totalPower++;
		}
		crystalTicks += crystalProp.get().cycles * PERIOD;
		while (crystalTicks >= crystalProp.get().seconds * 1000) {
			crystalTicks -= crystalProp.get().seconds * 1000;
			crystalTick();
			//totalCrystal++;
		}
	}
	
	public void shutdown() {
		exec.shutdownNow();
	}
}
