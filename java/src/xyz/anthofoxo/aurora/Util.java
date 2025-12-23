package xyz.anthofoxo.aurora;

import java.io.IOException;
import java.io.InputStream;

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
}
