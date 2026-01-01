package xyz.anthofoxo.aurora.struct.comp;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.annotation.KnownNativeName;

@KnownNativeName
public class PollComp implements Comp {

	/**
	 * LE: 8E 92 45 2C
	 */
	public static final int HASH = Hash.fnv1a("PollComp");

	public int hash = HASH;
	public int value;

	public PollComp() {
	}

	public PollComp(int value) {
		this.value = value;
	}

}
