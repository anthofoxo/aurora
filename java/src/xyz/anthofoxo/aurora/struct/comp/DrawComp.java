package xyz.anthofoxo.aurora.struct.comp;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.annotation.KnownNativeName;
import xyz.anthofoxo.aurora.struct.annotation.RemoveFieldIfEnclosed;
import xyz.anthofoxo.aurora.struct.experimental.UnknownSkyboxStruct;
import xyz.anthofoxo.aurora.struct.trait.TraitBucket;
import xyz.anthofoxo.aurora.struct.trait.TraitLayer;

@KnownNativeName
public class DrawComp implements Comp {
	/**
	 * LE: EE 19 27 F9 <br>
	 * BE: F9 27 19 EE
	 */
	public static final int HASH = Hash.fnv1a("DrawComp");

	public int hash = HASH;
	public int unknown0 = 8; // might be a kTraitAction which expects a boolean after?
	public boolean visible = true;
	public TraitLayer layer;
	public TraitBucket bucket;

	/**
	 * We arent super sure about this behavior yet, as of now it seems like only the
	 * skybox object can skip this
	 */
	@RemoveFieldIfEnclosed(clazz = UnknownSkyboxStruct.class)
	public int contextInt = 0;

	public DrawComp() {
	}

	public DrawComp(int unknown0, boolean visible, TraitLayer layer, TraitBucket bucket) {
		this.unknown0 = unknown0;
		this.visible = visible;
		this.layer = layer;
		this.bucket = bucket;
	}

}
