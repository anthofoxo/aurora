package xyz.anthofoxo.aurora.struct.comp;

import xyz.anthofoxo.aurora.Hash;

public class DSPParamEQ implements Comp {
	public static final int HASH = Hash.fnv1a("DSPParamEQ");

	public int hash = HASH;
	public int unknown0;

	public float unknown1;
	public float unknown2;
	public float unknown3;

	public boolean unknown4;

	public int hashDup; // Same as comp hash
}
