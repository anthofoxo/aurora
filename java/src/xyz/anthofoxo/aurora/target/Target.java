package xyz.anthofoxo.aurora.target;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

import imgui.type.ImBoolean;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.json.JsonMapper;
import xyz.anthofoxo.aurora.tml.TCLFile;

public abstract class Target {
	public static final JsonMapper JSON_MAPPER = JsonMapper.builder()
			.configure(JsonReadFeature.ALLOW_SINGLE_QUOTES, true).build();

	/**
	 * When a target is initialized, files must be iterated in order to determine
	 * compatibility, its recommended to load all files into memory until
	 * compilation time
	 */
	public Target(Path path) {
		this.path = path;
	}

	public TCLFile tcl;
	public Path path;
	public ImBoolean enabled = new ImBoolean(true);
	public int[] speedModifier = new int[] { 100 };

	public static class CompiledTarget {
		public HashMap<String, byte[]> pcFiles = new HashMap<>();
		public String levelName;
		public byte[] objlib;
		public byte[] sec;
		public String localizationKey;
		public String localizationValue;
	}

	public abstract CompiledTarget build(float speedModifier) throws IOException;

	/**
	 * Takes in a filepath and outputs the file extension without removing the dot
	 * Example: "myfile.leaf" -> ".leaf"
	 */
	public static String getExtension(String path) {
		String fileName = path.toLowerCase();
		int lastDotIndex = fileName.lastIndexOf('.');
		if (lastDotIndex == -1 || lastDotIndex == 0) return "";
		return fileName.substring(lastDotIndex);
	}
}
