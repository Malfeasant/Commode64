package us.malfeasant.commode64.config;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.google.gson.Gson;

public class Config {
	// TODO more options- VIC revision, ROM revisions, etc
	private final Power power;
	private final Oscillator oscillator;
	
	public static class Builder extends JPanel {
		private static final long serialVersionUID = 1L;
		private static final Config DEFAULT = new Config(Power.US, Oscillator.NTSC);
		private final JComboBox<Power> powCombo = new JComboBox<Power>(Power.values());
		private final JComboBox<Oscillator> oscCombo = new JComboBox<Oscillator>(Oscillator.values());
		private Config conf;
		
		public Builder() {
			this(DEFAULT);	// TODO persistent selectable defaults?
		}
		/**
		 * Essentially a copy constructor, since Config is immutable.
		 * @param c - an existing Config as a starting point to allow modification.
		 */
		public Builder(Config c) {
			conf = c;
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.insets = new Insets(2, 2, 2, 2);
			gbc.gridx = 0;	gbc.gridy = 0;
			add(new JLabel(Oscillator.class.getSimpleName()), gbc);
			gbc.gridx++;
			add(oscCombo, gbc);
			gbc.gridx = 0;	gbc.gridy++;
			add(new JLabel(Power.class.getSimpleName()), gbc);
			gbc.gridx++;
			add(powCombo, gbc);
			powCombo.setSelectedItem(c.power);
			oscCombo.setSelectedItem(c.oscillator);
		}
		public Config getConfig() {
			return conf;
		}
		public void showDialog(JComponent parent) {
			if (JOptionPane.showConfirmDialog(parent, this, "Options", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
				conf = new Config((Power) powCombo.getSelectedItem(), (Oscillator) oscCombo.getSelectedItem());
			}
		}
		public String pack() {
			Gson gson = new Gson();
			return gson.toJson(conf);
		}
		public void unpack(String s) {
			Gson gson = new Gson();
			conf = gson.fromJson(s, Config.class);
		}
	}
	
	
	private Config(Power pow, Oscillator osc) {
		power = pow;
		oscillator = osc;
	}
}
