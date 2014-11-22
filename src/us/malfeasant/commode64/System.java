package us.malfeasant.commode64;

import us.malfeasant.commode64.config.Config;

public class System {
	private final Config conf;
	public System() {
		this(Config.getDefault());
	}
	public System(Config c) {
		if (c == null) throw new NullPointerException("Use no-arg constructor!");
		conf = c;
	}
}
