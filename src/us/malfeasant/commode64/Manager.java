package us.malfeasant.commode64;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

class Manager {
	private static final Path home = Paths.get(System.getProperty("user.home"));
	
	public static void main(String[] args) {
		// TODO parse args... option to start a machine without manager?
		
		SwingUtilities.invokeLater(() -> new Manager());
	}
	
	private final JMenuItem settingsItem;
	private final JMenuItem cloneItem;
	private final JMenuItem deleteItem;
	
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
		item = new JMenuItem("New...");
		menu.add(item);
		item.addActionListener(event -> create());
		
		settingsItem = new JMenuItem("Settings...");
		menu.add(settingsItem);
		settingsItem.setEnabled(false);
		settingsItem.addActionListener(event -> settings());
		
		cloneItem = new JMenuItem("Clone...");
		menu.add(cloneItem);
		cloneItem.setEnabled(false);
		cloneItem.addActionListener(event -> copy());
		
		deleteItem = new JMenuItem("Delete...");
		menu.add(deleteItem);
		deleteItem.setEnabled(false);
		deleteItem.addActionListener(event -> delete());
		
		frame.setVisible(true);
		frame.pack();
	}
	
	private void create() {
		
	}
	private void settings() {
		
	}
	private void copy() {
		
	}
	private void delete() {
		
	}
}
