package xyz.anthofoxo.aurora;

import static org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class UserConfig {
	public static final String CONFIG_PATH = "aurora.properties";
	public static final Properties properties = new Properties();

	public static final List<String> modPaths = new ArrayList<>();

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

	public static String thumperPath() {
		var prop = properties.getProperty("thumper.path");
		if (prop != null) return prop;

		String v = tinyfd_openFileDialog("Select Thumper Executable", null, null, null, false);
		if (v == null) return null;

		v = Path.of(v).getParent().toString();
		properties.setProperty("thumper.path", v);
		save();
		return v;
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
