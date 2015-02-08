package us.malfeasant.commode64.cia;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ViewToD extends JPanel implements ChangeListener {
	private static final long serialVersionUID = 1L;
	private static final String FMT = "%02x:%02x:%02x.%01x %sm";
	private final JTextField[] fields;
	ViewToD() {
		super(new GridBagLayout());
		fields = new JTextField[3];
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(new JLabel("Latch:"), gbc);
		gbc.gridy++;
		add(new JLabel("Time:"), gbc);
		gbc.gridy++;
		add(new JLabel("Alarm:"), gbc);
		gbc.gridy++;
		gbc.gridx = 1;
		for (int i = 0; i < 3; i++) {
			gbc.gridy = i;
			fields[i] = new JTextField(8);
			fields[i].setEditable(false);
			add(fields[i], gbc);
		}
	}
	@Override
	public void updateLatch(int h, int m, int s, int t) {
		update(0, h, m, s, t);
	}
	@Override
	public void updateTime(int h, int m, int s, int t) {
		update(1, h, m, s, t);
	}
	@Override
	public void updateAlarm(int h, int m, int s, int t) {
		update(2, h, m, s, t);
	}
	private void update(int which, int h, int m, int s, int t) {
		fields[which].setText(String.format(FMT, h & 0x1f, m, s, t, (h & 0x80) == 0 ? "a" : "p" ));
	}
}
