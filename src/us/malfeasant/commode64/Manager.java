package us.malfeasant.commode64;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import us.malfeasant.commode64.config.Config;

public class Manager {
	private static final Path home = Paths.get(System.getProperty("user.home"), "Commode64");
	
	public static void main(String[] args) {
		// TODO parse args... option to start a machine without manager?
		
		SwingUtilities.invokeLater(() -> new Manager());
	}
	
	private final Action create = new AbstractAction("New") {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			create();
		}
	};
	private final Action settings = new AbstractAction("Settings") {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			settings();
		}
	};
	private final Action clone = new AbstractAction("Clone") {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			copy();
		}
	};
	private final Action delete = new AbstractAction("Delete") {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			delete();
		}
	};
	
	private Manager() {
		if (Files.notExists(home)) {
			try {
				Files.createDirectory(home);
			} catch (IOException e) {
				System.err.println("Fatal error: Storage directory missing, and could not be created.");	// TODO: try harder
				System.exit(-1);	// TODO: more graceful failure
			}
		}
		
		JFrame frame = new JFrame("Commode64");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JMenuBar bar = new JMenuBar();
		frame.setJMenuBar(bar);
		JMenu menu = new JMenu("File");
		bar.add(menu);
		JMenuItem item = new JMenuItem("Exit");
		menu.add(item);
		item.addActionListener(event -> frame.dispose());
		
		menu = new JMenu("Machine");
		bar.add(menu);
		item = new JMenuItem();
		menu.add(item);
		item.setAction(create);
		
		item = new JMenuItem();
		menu.add(item);
		settings.setEnabled(false);
		item.setAction(settings);
		
		item = new JMenuItem();
		menu.add(item);
		clone.setEnabled(false);
		item.setAction(clone);
		
		item = new JMenuItem();
		menu.add(item);
		delete.setEnabled(false);
		item.setAction(delete);
		
		frame.setVisible(true);
		frame.pack();
	}
	
	private void create() {
		Path confFile = null;
		while (confFile == null) {
			String name = JOptionPane.showInputDialog(null, "Choose a name for your machine:", "");
			confFile = home.resolve(name + ".c64");
			if (validate(name) && !Files.exists(confFile)) {
				try {
					Files.createFile(confFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					confFile = null;
				}
			} else {
				confFile = null;
				JOptionPane.showMessageDialog(null, "No good!", "Error", JOptionPane.ERROR_MESSAGE);	// TODO: obvious?
			}
		}
		Config.Builder cb = new Config.Builder();
		cb.showDialog(null);	// TODO: pass a component once we have some to choose from...
		System.out.println(cb.pack());
	}
	private void settings() {
		
	}
	private void copy() {
		
	}
	private void delete() {
		
	}
	private boolean validate(String filename) {
		if (filename.contains("/")) return false;
		if (filename.contains("\\")) return false;	// TODO: better way?
		if (filename.contains("?")) return false;
		if (filename.contains(":")) return false;
		return true;
	}
}
