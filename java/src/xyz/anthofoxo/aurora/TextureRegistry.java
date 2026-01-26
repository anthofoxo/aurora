package xyz.anthofoxo.aurora;

import java.util.HashMap;

import xyz.anthofoxo.aurora.gfx.Texture;

public final class TextureRegistry {
	private static final HashMap<String, Texture> TEXTURES = new HashMap<>();

	private TextureRegistry() {
	}

	public static Texture get(String resource) {
		var asset = TEXTURES.get(resource);
		if (asset != null) return asset;

		asset = Texture.makeFromResource("textures/" + resource);
		TEXTURES.put(resource, asset);
		return asset;
	}

	public static void close() {
		TEXTURES.values().forEach(t -> {
			if (t != null) t.close();
		});
	}
}
