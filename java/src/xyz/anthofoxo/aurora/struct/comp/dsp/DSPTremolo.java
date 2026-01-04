package xyz.anthofoxo.aurora.struct.comp.dsp;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class DSPTremolo implements Comp {
	public static final int HASH = Hash.fnv1a("DSPTremolo");

	public int hash = HASH;

	public int unknown0;

	@FixedSize(count = 8)
	public float[] unknown1;

	public boolean unknown2;

	public int hashDup;
}
