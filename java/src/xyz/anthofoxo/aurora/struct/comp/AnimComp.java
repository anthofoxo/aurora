package xyz.anthofoxo.aurora.struct.comp;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.annotation.KnownNativeName;
import xyz.anthofoxo.aurora.struct.state.Time;

@KnownNativeName
public class AnimComp implements Comp {
	public static final int HASH = Hash.fnv1a("AnimComp");

	public int hash = HASH;
	public int unknown0 = 1;
	public float unknown1 = 0.0f;
	public Time timeUnit = Time.kTimeBeats;
}
