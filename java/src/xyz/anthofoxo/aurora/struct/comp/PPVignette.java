package xyz.anthofoxo.aurora.struct.comp;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;

public class PPVignette implements Comp {
	public static final int HASH = Hash.fnv1a("PPVignette");

	public int hash = HASH;
	public int unknown0;
	@FixedSize(count = 14)
	public float[] unknown1;
	public boolean unknown2;

	// Should be a duplicate of the comp hash
	public int hashDup;
}
