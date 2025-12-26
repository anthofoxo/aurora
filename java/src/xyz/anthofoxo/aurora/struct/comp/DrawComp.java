package xyz.anthofoxo.aurora.struct.comp;

import xyz.anthofoxo.aurora.Hash;

public class DrawComp implements Comp {
	/**
	 * LE: EE 19 27 F9 <br>
	 * BE: F9 27 19 EE
	 */
	public static final int HASH = Hash.fnv1a("DrawComp");

	public int hash = HASH;
	public int unknown0 = 8;
	public boolean unknown1 = true;
	public String drawLayers = "kNumDrawLayers";
	public String parent = "kBucketParent";
	public int unknown2 = 0;
}
