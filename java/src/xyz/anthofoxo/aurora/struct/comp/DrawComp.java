package xyz.anthofoxo.aurora.struct.comp;

import java.util.List;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.UnknownSkyboxStruct;
import xyz.anthofoxo.aurora.struct.annotation.RemoveFieldIfEnclosed;
import xyz.anthofoxo.aurora.struct.trait.TraitBucket;
import xyz.anthofoxo.aurora.struct.trait.TraitLayer;

public class DrawComp implements Comp {
	/**
	 * LE: EE 19 27 F9 <br>
	 * BE: F9 27 19 EE
	 */
	public static final int HASH = Hash.fnv1a("DrawComp");

	public int hash = HASH;
	public int unknown0; // typically 8; might be a kTraitAction
	public boolean visible;
	public TraitLayer layer;
	public TraitBucket bucket;

	/**
	 * This field must be removed if parsing a skybox. This field otherwise usually
	 * is empty. In the level objlib footer this typically has 2 string pointing to
	 * a couple objects
	 */
	@RemoveFieldIfEnclosed(clazz = UnknownSkyboxStruct.class)
	public List<String> contextInt;

	public DrawComp() {
	}

	public DrawComp(int unknown0, boolean visible, TraitLayer layer, TraitBucket bucket, List<String> refs) {
		this.unknown0 = unknown0;
		this.visible = visible;
		this.layer = layer;
		this.bucket = bucket;
		this.contextInt = refs;
	}

}
