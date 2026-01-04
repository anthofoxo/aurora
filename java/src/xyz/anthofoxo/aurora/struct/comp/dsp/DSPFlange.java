package xyz.anthofoxo.aurora.struct.comp.dsp;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class DSPFlange implements Comp {
	/**
	 * LE: 03 48 AD 32
	 */
	public static final int HASH = Hash.fnv1a("DSPFlange");

	public int hash = HASH;
	public int unknown0;
	public float unknown1;
	public float unknown2;
	public float unknown3;
	public boolean unknown4;
	public int hashDup;
}
