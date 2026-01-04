package xyz.anthofoxo.aurora.struct.comp;

import xyz.anthofoxo.aurora.Hash;

public class DSPCompressor implements Comp {
	public static final int HASH = Hash.fnv1a("DSPCompressor");

	public int hash = HASH;

	public int unknown0;

	public float unknown1;
	public float unknown2;
	public float unknown3;
	public float unknown4;
	public float unknown5;
	public boolean unknown6;
	public boolean unknown7;

	public int hashDup;

}
