package xyz.anthofoxo.aurora.struct.trait;

import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.parse.AuroraWriter;
import xyz.anthofoxo.aurora.struct.ThumperStruct;
import xyz.anthofoxo.aurora.struct.annotation.UnknownNativeName;

@UnknownNativeName
public enum TraitBucket implements ThumperStruct {
	kBucketTerrain, kBucketMain, kBucketEffect, kBucketPostEffect, kBucketParent;

	public static TraitBucket in(AuroraReader in) {
		return valueOf(in.str());
	}

	public static void out(AuroraWriter out, TraitBucket v) {
		out.str(v.name());
	}
}
