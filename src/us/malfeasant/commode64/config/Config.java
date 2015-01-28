package us.malfeasant.commode64.config;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.google.gson.Gson;

public class Config {
	// TODO more options- VIC revision, ROM revisions, etc
	public final Power power;
	public final Oscillator oscillator;
	
	public static class Builder extends JPanel {
		private static final long serialVersionUID = 1L;
		private static final Config DEFAULT_CONF = new Config(Power.US, Oscillator.NTSC);
		private final JComboBox<Power> powCombo = new JComboBox<Power>(Power.values());
		private final JComboBox<Oscillator> oscCombo = new JComboBox<Oscillator>(Oscillator.values());
		private Config conf = DEFAULT_CONF;
		
		public Builder() {
			super(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			//gbc.weightx = 1;
			gbc.insets = new Insets(2, 2, 2, 2);
			gbc.gridx = 0;	gbc.gridy = 0;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			JLabel lab = new JLabel("Easy:");
			lab.setHorizontalAlignment(SwingConstants.CENTER);
			add(lab, gbc);
			gbc.gridwidth = 1;
			gbc.gridy++;
			JButton but = new JButton(Oscillator.NTSC.toString());
			but.addActionListener(event -> makeUS());
			add(but, gbc);
			gbc.gridx++;
			but = new JButton(Oscillator.PAL.toString());
			but.addActionListener(event -> makeEU());
			add(but, gbc);
			gbc.gridx = 0;	gbc.gridy++;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			add(new JPanel(), gbc);	// spacer
			gbc.gridx = 0;	gbc.gridy++;
			lab = new JLabel("Hard:");
			lab.setHorizontalAlignment(SwingConstants.CENTER);
			add(lab, gbc);
			gbc.gridwidth = 1;
			gbc.gridy++;
			lab = new JLabel(Oscillator.class.getSimpleName());
			lab.setHorizontalAlignment(SwingConstants.TRAILING);
			add(lab, gbc);
			gbc.gridx++;
			add(oscCombo, gbc);
			gbc.gridx = 0;	gbc.gridy++;
			lab = new JLabel(Power.class.getSimpleName());
			lab.setHorizontalAlignment(SwingConstants.TRAILING);
			add(lab, gbc);
			gbc.gridx++;
			add(powCombo, gbc);
			powCombo.setSelectedItem(conf.power);
			oscCombo.setSelectedItem(conf.oscillator);
		}
		private void makeUS() {
			powCombo.setSelectedItem(Power.US);
			oscCombo.setSelectedItem(Oscillator.NTSC);
		}
		private void makeEU() {
			powCombo.setSelectedItem(Power.EU);
			oscCombo.setSelectedItem(Oscillator.PAL);
		}
		public Config getConfig() {
			return conf;
		}
		/**
		 * @param parent: component to display near
		 * @return true if OK was clicked, false otherwise.
		 */
		public void showDialog() {
			JOptionPane.showConfirmDialog(null, this, "Options", JOptionPane.DEFAULT_OPTION);
			conf = new Config((Power) powCombo.getSelectedItem(), (Oscillator) oscCombo.getSelectedItem());
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
