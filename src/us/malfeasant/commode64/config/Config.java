package us.malfeasant.commode64.config;

import java.util.prefs.Preferences;

import com.google.gson.Gson;

public class Config {
	private static final Preferences prefs = Preferences.userNodeForPackage(Config.class);
	// TODO more options- VIC revision, ROM revisions, etc
	private final Power pow;
	private final Oscillator osc;
	
	public static Config getDefault() {
		return new Config();
	}
	public static Config unpack(String s) {
		Gson gson = new Gson();
		return gson.fromJson(s, Config.class);
	}
	
	private Config() {
		pow = Power.valueOf(prefs.get(Power.class.getSimpleName(), Power.US.name()));
		osc = Oscillator.valueOf(prefs.get(Oscillator.class.getSimpleName(), Oscillator.NTSC.name()));
	}
	private Config(Power p, Oscillator o) {
		pow = p;
		osc = o;
	}
	
	public Config withPower(Power p) {
		return new Config(p, osc);
	}
	public Config withOscillator(Oscillator o) {
		return new Config(pow, o);
	}
	public String pack() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
