package xyz.anthofoxo.aurora.struct.trait;

import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.parse.AuroraWriter;
import xyz.anthofoxo.aurora.struct.ThumperStruct;
import xyz.anthofoxo.aurora.struct.annotation.UnknownNativeName;

@UnknownNativeName
public enum TraitLayer implements ThumperStruct {
	kLayerWorld, kLayerUI, kNumDrawLayers;

	public static TraitLayer in(AuroraReader in) {
		return valueOf(in.str());
	}

	public static void out(AuroraWriter out, TraitLayer v) {
		out.str(v.name());
	}
}
