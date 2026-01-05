package xyz.anthofoxo.aurora.target;

import java.io.IOException;
import java.util.HashMap;

import imgui.type.ImBoolean;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.json.JsonMapper;
import xyz.anthofoxo.aurora.gfx.Texture;
import xyz.anthofoxo.aurora.tml.TCLFile;

public abstract class Target {
	public static final JsonMapper JSON_MAPPER = JsonMapper.builder()
			.configure(JsonReadFeature.ALLOW_SINGLE_QUOTES, true).build();

	public final String origin;

	public TCLFile tcl;
	public ImBoolean enabled = new ImBoolean(true);
	public int[] speedModifier = new int[] { 100 };
	public Texture texture;

	public Target(String origin) {
		this.origin = origin;
	}

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
