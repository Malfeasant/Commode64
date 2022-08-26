package us.malfeasant.commode64;

import java.util.prefs.Preferences;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import us.malfeasant.commode64.machine.video.Variant;
import us.malfeasant.commode64.timing.Crystal;
import us.malfeasant.commode64.timing.Power;

/**
 * Holds the configuration bits as a set of properties.  Uses factory methods to create a few basic configurations.
 * Gives out read-only copies of the properties for adding listeners, so this is the only place changes can be made.
 * TODO: add ui components to set these properties.
 * @author Malfeasant
 */
public class Config {
	private static final Preferences prefs = Preferences.userNodeForPackage(Config.class);
	
	private final ReadOnlyObjectWrapper<Crystal> crystalProp;
	private final ReadOnlyObjectWrapper<Power> powerProp;
	private final ReadOnlyObjectWrapper<Variant> variantProp;
	
	private Config(Crystal c, Power p, Variant v) {
		if (c == null || p == null || v == null) throw new NullPointerException();
		crystalProp = new ReadOnlyObjectWrapper<>(c);
		powerProp = new ReadOnlyObjectWrapper<>(p);
		variantProp = new ReadOnlyObjectWrapper<>(v);
	}
	
	public static Config getDefault() {
    	Crystal crys = Crystal.valueOf(prefs.get(Crystal.class.getSimpleName(), Crystal.NTSC.name()));
    	Power pow = Power.valueOf(prefs.get(Power.class.getSimpleName(), Power.NA.name()));
    	Variant var = Variant.valueOf(prefs.get(Variant.class.getSimpleName(), Variant.NTSC_NEW.name()));
		return new Config(crys, pow, var);
	}
	public static Config getNTSC() {
		return new Config(Crystal.NTSC, Power.NA, Variant.NTSC_NEW);
	}
	public static Config getPAL() {
		return new Config(Crystal.PAL, Power.EU, Variant.PAL_NEW);
	}
	
	public void setCrystal(Crystal c) {
		if (c == null) throw new NullPointerException();
		crystalProp.set(c);
	}
	public void setPower(Power p) {
		if (p == null) throw new NullPointerException();
		powerProp.set(p);
	}
	public void setVariant(Variant v) {
		if (v == null) throw new NullPointerException();
		variantProp.set(v);
	}
	public void setAsDefault() {
		prefs.put(Crystal.class.getSimpleName(), crystalProp.get().name());
		prefs.put(Power.class.getSimpleName(), powerProp.get().name());
		prefs.put(Variant.class.getSimpleName(), variantProp.get().name());
	}
	
	public ReadOnlyObjectProperty<Crystal> crystalProperty() {
		return crystalProp.getReadOnlyProperty();
	}
	public ReadOnlyObjectProperty<Power> powerProperty() {
		return powerProp.getReadOnlyProperty();
	}
	public ReadOnlyObjectProperty<Variant> variantProperty() {
		return variantProp.getReadOnlyProperty();
	}
}
