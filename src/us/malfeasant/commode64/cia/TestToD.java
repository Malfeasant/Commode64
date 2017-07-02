package us.malfeasant.commode64.cia;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * This only tests proper functionality- testing improper functionality (i.e. writing nonsense values) can wait.
 * @author Mischa
 * TODO - rewrite tests to work with reworked class
 */
public class TestToD {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new TestToD());
	}
	TestToD() {
		JFrame frame = new JFrame("Time of Day tester");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel(new GridBagLayout());
		frame.add(panel);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		JButton irq = new JButton("IRQ");
		irq.addActionListener(e -> irq.setEnabled(false));
		panel.add(irq, gbc);
		
		ToD tod = new ToD() {
			@Override
			void setIRQ() {
				irq.setEnabled(true);
			}
		};
		
		gbc.gridy = 1;
		JButton tickBut = new JButton("Tick");
		tickBut.addActionListener(e -> tod.tick());
		panel.add(tickBut, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 2;
//		ViewToD view = new ViewToD();
//		panel.add(view, gbc);
//		tod.addListener(view);
//		tod.set(0, 0, false);
		/*
		tod.set(3, 0x12, true);
		for (int i = 0; i < 0x100; i++) {
			tod.tick();
			System.out.println("i = " + i + ":");
			switch (i) {
			case 0x8c:
			case 0x4c:
			case 0xc:
				System.out.println("Starting clock:");
				tod.set(0, 0, false);
				break;
			case 0x40:
				System.out.println("Setting hours to 11am:");
				tod.set(3, 0x11, false);
				break;
			case 0xa4:
			case 0x64:
			case 0x84:
			case 0x44:
				System.out.println("Setting minutes to 59:");
				tod.set(2, 0x59, false);
				break;
			case 0xa8:
			case 0x68:
			case 0x88:
			case 0x48:
				System.out.println("Setting seconds to 59:");
				tod.set(1, 0x59, false);
				break;
			case 0x80:
				System.out.println("Setting hours to 11pm:");
				tod.set(3, 0x91, false);
				break;
			}
			System.out.println(tod.debug());
		}*/
		frame.pack();
		frame.setVisible(true);
	}
}
