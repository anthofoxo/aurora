package xyz.anthofoxo.aurora.struct.comp;

import java.util.List;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.ThumperStruct;

public class EntityComp implements Comp {
	// LE: CF 60 C6 ED
	public static final int HASH = Hash.fnv1a("EntityComp");

	public static class AnimEntry implements ThumperStruct {
		public String anim;
		public boolean unknown0;
	}

	public int hash = HASH;
	public int unknown0; // 8

	public List<AnimEntry> anims; // approach_sounds_left.anim

	public List<String> flows; // turn_left_isolated.flow
	public String sequin; // kSequinTurnLeft
	public String base; // base.ent
}
