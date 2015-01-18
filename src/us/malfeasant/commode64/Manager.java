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
import javax.swing.SwingUtilities;

import us.malfeasant.commode64.config.Config;

public class Manager {
	private static final Path home = Paths.get(System.getProperty("user.home"));
	
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
		Path path = home.resolve("Commode64");
		if (Files.notExists(path)) {
			try {
				Files.createDirectory(path);
			} catch (IOException e) {
				System.err.println("Fatal error: Storage directory doesn't exist, and couldn't be created.");
				System.exit(-1);
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
}
