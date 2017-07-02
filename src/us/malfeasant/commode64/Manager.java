package us.malfeasant.commode64;

import javax.swing.SwingUtilities;

import us.malfeasant.commode64.config.Config;

public class Manager {
	public static void main(String[] args) {
		// TODO parse args... pass config options to forego dialog?
		
		SwingUtilities.invokeLater(() -> new Manager());
	}
	
	private final Config conf;
	
	private Manager() {
		Config.Builder builder = new Config.Builder();
		builder.showDialog();
		conf = builder.getConfig();
	}
	
	private Manager(Config conf) {
		if (conf == null) throw new NullPointerException(getClass() + " constructor called with null Config!");
		this.conf = conf;
	}
}
