package xyz.anthofoxo.aurora;

import java.awt.Desktop;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Util {

	static {
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
		});
	}

	public static String getStackTraceAsString(Throwable throwable) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		throwable.printStackTrace(pw);
		return sw.toString(); // stack trace as a string
	}

	public static JFrame makeOnTopParent() {
		JFrame parent = new JFrame();
		parent.setAlwaysOnTop(true);
		parent.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		parent.setUndecorated(true);
		parent.setLocationRelativeTo(null);
		parent.setVisible(true);
		return parent;
	}

	public static int showOptionDialog(String message, String title, int optionType, int messageType) {
		var runnable = new Runnable() {
			public int returnValue;

			public void run() {
				var parent = makeOnTopParent();

				try {
					returnValue = JOptionPane.showOptionDialog(parent, message, title, optionType, messageType, null,
							null, 0);
				} finally {
					parent.dispose();
				}
			}
		};

		try {
			SwingUtilities.invokeAndWait(runnable);
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}

		return runnable.returnValue;
	}

	public static InputStream getResource(String resource) {
		try {
			return new FileInputStream(resource);
		} catch (FileNotFoundException e) {
		}

		return Util.class.getClassLoader().getResourceAsStream(resource);
	}

	public static byte[] getResourceBytes(String resource) throws IOException {
		try (var stream = getResource(resource)) {
			if (stream == null) throw new IOException("Resource not found: " + resource);
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

	public static List<Path> getAllFilesFromResourceDirectory(String directoryPath)
			throws URISyntaxException, IOException {
		URL url = Util.class.getClassLoader().getResource(directoryPath);
		if (url == null) {
			throw new IllegalArgumentException("Resource directory not found: " + directoryPath);
		}

		URI uri = url.toURI();
		Path dirPath;
		FileSystem fileSystem = null;

		if ("jar".equals(uri.getScheme())) {
			fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
			dirPath = fileSystem.getPath(directoryPath);
		} else {
			dirPath = Paths.get(uri);
		}

		List<Path> files;

		try (Stream<Path> walk = Files.walk(dirPath)) {
			files = walk.filter(Files::isRegularFile).collect(Collectors.toList());
		} finally {
			if (fileSystem != null) {
				fileSystem.close();
			}
		}

		return files;
	}
}
