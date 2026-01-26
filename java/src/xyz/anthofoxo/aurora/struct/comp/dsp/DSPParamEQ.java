package xyz.anthofoxo.aurora.struct.comp.dsp;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class DSPParamEQ implements Comp {
	public static final int HASH = Hash.fnv1a("DSPParamEQ");

	public int hash = HASH;
	public int unknown0;

	public float cutoffFrequency;
	public float unknown2;
	public float volumeGain;

	public boolean unknown4;

	public int hashDup; // Same as comp hash
}
