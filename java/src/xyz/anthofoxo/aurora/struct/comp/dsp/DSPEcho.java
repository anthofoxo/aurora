package xyz.anthofoxo.aurora.struct.comp.dsp;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.comp.Comp;

/**
 * LE: 8A 28 DC B5
 */
public class DSPEcho implements Comp {
	public static final int HASH = Hash.fnv1a("DSPEcho");
	
	public int hash = HASH;
	public int unknown0;
	public float unknown1;
	public float unknown2;
	public float unknown3;
	public float unknown4;
	public boolean unknown5;
	public int hashDup; // SAME AS COMP HASH ABOVE
	
}
