package xyz.anthofoxo.aurora.struct.comp;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.Comp;
import xyz.anthofoxo.aurora.struct.state.Time;

public class AnimComp implements Comp {
	public int hash = Hash.fnv1a("AnimComp");
	public int unknown0 = 1;
	public float unknown1 = 0.0f;
	public Time timeUnit = Time.kTimeBeats;
}
