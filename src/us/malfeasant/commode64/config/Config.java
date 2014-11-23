package us.malfeasant.commode64.config;

/**
 * Holds in one place whatever options aren't expected to change in a running system- 
 * chip revisions, clock rate...
 */
public class Config {	// TODO make more meaningful
	private static Config DEFAULT = new Config();
	
	public static Config getDefault() {
		return DEFAULT;
	}
	
	private Config() {
		
	}
}
