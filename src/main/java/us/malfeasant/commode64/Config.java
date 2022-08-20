package us.malfeasant.commode64;

import java.util.prefs.Preferences;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import us.malfeasant.commode64.timing.Crystal;
import us.malfeasant.commode64.timing.Power;

/**
 * Holds the configuration bits as a set of properties.  Uses factory methods to create a few basic configurations.
 * Gives out read-only copies of the properties for adding listeners, so this is the only place changes can be made.
 * @author Malfeasant
 */
public class Config {
	private static final Preferences prefs = Preferences.userNodeForPackage(Config.class);
	
	private final ReadOnlyObjectWrapper<Crystal> crystalProp;
	private final ReadOnlyObjectWrapper<Power> powerProp;
	
	private Config(Crystal c, Power p) {
		if (c == null || p == null) throw new NullPointerException();
		crystalProp = new ReadOnlyObjectWrapper<>(c);
		powerProp = new ReadOnlyObjectWrapper<>(p);
	}
	
	public static Config getDefault() {
    	Crystal crys = Crystal.valueOf(prefs.get(Crystal.class.getSimpleName(), Crystal.NTSC.name()));
    	Power pow = Power.valueOf(prefs.get(Power.class.getSimpleName(), Power.NA.name()));
		return new Config(crys, pow);
	}
	public static Config getNTSC() {
		return new Config(Crystal.NTSC, Power.NA);
	}
	public static Config getPAL() {
		return new Config(Crystal.PAL, Power.EU);
	}
	
	public void setCrystal(Crystal c) {
		if (c == null) throw new NullPointerException();
		crystalProp.set(c);
	}
	public void setPower(Power p) {
		if (p == null) throw new NullPointerException();
		powerProp.set(p);
	}
	public void setAsDefault() {
		prefs.put(Crystal.class.getSimpleName(), crystalProp.get().name());
		prefs.put(Power.class.getSimpleName(), powerProp.get().name());
	}
	
	public ReadOnlyObjectProperty<Crystal> crystalProperty() {
		return crystalProp.getReadOnlyProperty();
	}
	public ReadOnlyObjectProperty<Power> powerProperty() {
		return powerProp.getReadOnlyProperty();
	}
}
