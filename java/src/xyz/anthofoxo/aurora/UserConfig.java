package xyz.anthofoxo.aurora;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class UserConfig {
	public static final String CONFIG_PATH = "aurora.properties";
	public static final Properties properties = new Properties();

	public static final List<String> modPaths = new ArrayList<>();

	private static final String[] PREINSTALLED_THUMPER_LOCATIONS = {
			"C:/Program Files (x86)/Steam/steamapps/common/Thumper", // Windows
			"~/.local/share/Steam/steamapps/common/Thumper", // Linux
	};

	static {
		try {
			properties.load(new FileReader(new File(CONFIG_PATH)));
		} catch (FileNotFoundException e) {
			// config file wasnt found, this is okay, the default properties are fine
		} catch (IOException e) {
			e.printStackTrace();
		}

		var prop = properties.getProperty("aurora.mod_paths");

		if (prop != null && !prop.isEmpty()) {
			modPaths.addAll(List.of(prop.split(",")));
		}

	}

	public static void set(String key, String value) {
		properties.setProperty(key, value);
		save();
	}

	public static String get(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	public static boolean isUnlockPractice() {
		return Boolean.parseBoolean(properties.getProperty("aurora.unlock_practice", Boolean.toString(true)));
	}

	public static void setUnlockPractice(boolean unlock) {
		properties.setProperty("aurora.unlock_practice", Boolean.toString(unlock));
		save();
	}

	public static boolean isModEnabled(String modName) {
		return Boolean
				.parseBoolean(properties.getProperty(String.format("mod.%s.enabled", modName), Boolean.toString(true)));
	}

	public static void setModEnabled(String modName, boolean enabled) {
		properties.setProperty(String.format("mod.%s.enabled", modName), Boolean.toString(enabled));
		save();
	}

	/**
	 * Prompts the user for their thumper path and if one is chosen, it'll be saved
	 * in the user config settings.
	 */
	public static void pickAndSaveThumperPath() {
		var path = pickThumperPath();
		if (path != null) {
			properties.setProperty("thumper.path", path);
			save();
		}
	}

	/**
	 * Searches a few common locations for the Thumper installation. If one is found
	 * the user will be prompted if they want this path to be used. If they choose
	 * no or a default wasn't found then they will be prompted to search for it
	 * themselves. The return value of this function is the chosen directory or
	 * <code>null</code> if the dialog was cancelled.
	 */
	public static String pickThumperPath() {
		String path = null;

		for (var preinstalledPath : PREINSTALLED_THUMPER_LOCATIONS) {
			try {
				if (Files.exists(Path.of(preinstalledPath))) {
					path = preinstalledPath;
					break;
				}
			} catch (Throwable e) {
			}
		}

		if (path != null) {
			if (JOptionPane.YES_OPTION == Util.showOptionDialog(
					"Thumper installation found, Should Aurora use this directory?", "Thumper Installation Found",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
				return path;
			} else path = null;
		}

		var runnable = new Runnable() {
			public String path;

			public void run() {
				var chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					path = chooser.getSelectedFile().toString();
				}
			}
		};

		try {
			SwingUtilities.invokeAndWait(runnable);
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}

		if (runnable.path != null) return runnable.path;
		return null;
	}

	public static String thumperPath() {
		return properties.getProperty("thumper.path", null);
	}

	public static void save() {
		try {
			properties.setProperty("aurora.mod_paths", String.join(",", modPaths));
			properties.store(new FileOutputStream(new File("aurora.properties")), "Aurora config");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean shouldShowGuide() {
		return Boolean.parseBoolean(properties.getProperty("aurora.show_guide", Boolean.toString(true)));
	}

	public static void setShowGuide(boolean b) {
		properties.setProperty("aurora.show_guide", Boolean.toString(b));
		save();
	}
}
