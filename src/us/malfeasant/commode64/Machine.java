package us.malfeasant.commode64;

import us.malfeasant.commode64.config.Config;

public class Machine {
	private final Config conf;
	public Machine() {
		this(Config.getDefault());
	}
	public Machine(Config c) {
		if (c == null) throw new NullPointerException("Use no-arg constructor!");
		conf = c;
	}
}
