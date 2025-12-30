package xyz.anthofoxo.aurora;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class Util {
	public static InputStream getResource(String resource) {
		return Util.class.getClassLoader().getResourceAsStream(resource);
	}

	public static byte[] getResourceBytes(String resource) throws IOException {
		try (var stream = getResource(resource)) {
			if (stream == null) throw new IOException("Resource not found");
			return stream.readAllBytes();
		}
	}

	public static boolean openURL(String url) {
		if (!Desktop.isDesktopSupported()) {
			System.err.println("Desktop API is not supported on the current platform.");
			return false;
		}

		Desktop desktop = Desktop.getDesktop();

		if (!desktop.isSupported(Desktop.Action.BROWSE)) {
			System.err.println("Browse action is not supported on the current platform.");
			return false;
		}

		try {
			desktop.browse(new URI(url));
			System.out.println("Opened URL: " + url + " in default browser.");
			return true;
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			return false;
		}

	}
}
