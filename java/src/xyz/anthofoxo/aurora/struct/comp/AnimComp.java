package xyz.anthofoxo.aurora.struct.comp;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.annotation.KnownNativeName;
import xyz.anthofoxo.aurora.struct.trait.Time;

@KnownNativeName
public class AnimComp implements Comp {
	/**
	 * LE: 0A 9F 25 63
	 */
	public static final int HASH = Hash.fnv1a("AnimComp");

	public int hash = HASH;
	public int unknown0 = 1;
	public float unknown1 = 0.0f;
	public Time timeUnit = Time.kTimeBeats;
}
