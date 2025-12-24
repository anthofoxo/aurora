package xyz.anthofoxo.aurora;

import static org.lwjgl.util.tinyfd.TinyFileDialogs.tinyfd_openFileDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class UserConfig {
	public static final Properties properties = new Properties();

	static {
		try (var resource = Util.getResource("config.properties")) {
			if (resource != null) properties.load(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			properties.store(new FileOutputStream(new File("aurora_res/config.properties")), "Aurora config");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
