package xyz.anthofoxo.aurora.struct.trait;

import xyz.anthofoxo.aurora.struct.ThumperStruct;
import xyz.anthofoxo.aurora.struct.annotation.UnknownNativeName;

@UnknownNativeName
public class TraitBucket implements ThumperStruct {
	public static final TraitBucket Terrain = new TraitBucket("kBucketTerrain");
	public static final TraitBucket Main = new TraitBucket("kBucketMain");
	public static final TraitBucket Effect = new TraitBucket("kBucketEffect");
	public static final TraitBucket PostEffect = new TraitBucket("kBucketPostEffect");
	public static final TraitBucket Parent = new TraitBucket("kBucketParent");

	public String value;

	public TraitBucket() {
	}

	public TraitBucket(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
