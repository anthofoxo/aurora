package xyz.anthofoxo.aurora.gfx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import imgui.ImFont;
import imgui.ImFontConfig;
import imgui.ImGui;
import xyz.anthofoxo.aurora.EntryPoint;
import xyz.anthofoxo.aurora.Util;

public final class Font {
	public static final float DEFAULT_SIZE = 18.0f;
	public static final String DEFAULT = "default";

	private static final ArrayList<byte[]> fontMemories = new ArrayList<>();
	private static final HashMap<String, ImFont> fonts = new HashMap<>();

	private Font() {
	}

	/**
	 * Fonts MUST be registered at the start of the application. Given a resource
	 * location, a string to identify this font later and a font size
	 * 
	 * @see EntryPoint#imGuiInit()
	 */
	public static void registerFont(String resource, String key, float size) {
		try (var stream = Util.getResource("NotoSans-Regular.ttf")) {
			byte[] ttf = stream.readAllBytes();
			ImFontConfig cfg = new ImFontConfig();
			cfg.setFontDataOwnedByAtlas(false);
			ImFont font = ImGui.getIO().getFonts().addFontFromMemoryTTF(ttf, size, cfg);

			fonts.put(key, font);
			fontMemories.add(ttf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Searches for a font with a specific key, If the font is not found then the
	 * current font is returned
	 */
	public static ImFont getFont(String key) {
		return fonts.getOrDefault(key, ImGui.getFont());
	}

}
