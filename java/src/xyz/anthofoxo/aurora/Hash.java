package xyz.anthofoxo.aurora;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.joml.Math;

import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.yaml.YAMLMapper;

public final class Hash {
	public static HashMap<Integer, byte[]> hashes = new HashMap<>();

	static {
		reloadHashes();
	}

	private Hash() {
	}

	private static final class HashesContainer {
		public List<String> values;
	}

	public static byte[] escapedToByteArray(String escaped) {
		ByteArrayOutputStream out = new ByteArrayOutputStream(escaped.length());

		for (int i = 0; i < escaped.length();) {
			char c = escaped.charAt(i);

			if (c == '\\') {
				if (i + 3 < escaped.length() && escaped.charAt(i + 1) == 'x') {
					int hi = Character.digit(escaped.charAt(i + 2), 16);
					int lo = Character.digit(escaped.charAt(i + 3), 16);

					if (hi == -1 || lo == -1) {
						throw new IllegalArgumentException("Invalid hex escape at index " + i + ": "
								+ escaped.substring(i, Math.min(i + 4, escaped.length())));
					}

					out.write((hi << 4) | lo);
					i += 4;
					continue;
				}

				// Invalid escape sequence at index, parse as a literal \ and continue
				out.write((byte) '\\');
				++i;
			}

			if (c > 0x7F) {
				throw new IllegalArgumentException("Non-ASCII character '" + c + " at index " + i);
			}

			out.write((byte) c);
			++i;
		}

		return out.toByteArray();
	}

	public static void reloadHashes() {
		hashes.clear();

		try (var stream = Util.getResource("hashes.yaml")) {
			YAMLMapper mapper = new YAMLMapper();
			var object = mapper.readValue(stream.readAllBytes(), HashesContainer.class);

			for (var inputEscaped : object.values) {
				var input = escapedToByteArray(inputEscaped);
				var hash = fnv1a(input);

				hashes.put(hash, input);
			}

		} catch (IOException | JacksonException e) {
			e.printStackTrace();
		}
	}

	public static int fnv1a(String string) {
		return fnv1a(string.getBytes());
	}

	public static int fnv1a(byte[] values) {
		int hash = 0x811c9dc5;

		for (byte value : values) {
			hash = (hash ^ (value & 0xFF)) * 0x1000193;
		}

		hash *= 0x2001;
		hash = hash ^ (hash >>> 0x7);
		hash *= 0x9;
		hash = hash ^ (hash >>> 0x11);
		hash *= 0x21;

		return hash;
	}
}
