package xyz.anthofoxo.aurora.struct.trait;

import xyz.anthofoxo.aurora.struct.annotation.UnknownNativeName;

@UnknownNativeName
public class TraitLayer {
	public static final TraitLayer World = new TraitLayer("kLayerWorld");
	public static final TraitLayer UI = new TraitLayer("kLayerUI");
	public static final TraitLayer NumLayers = new TraitLayer("kNumDrawLayers");

	public String value;

	public TraitLayer() {
	}

	public TraitLayer(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
