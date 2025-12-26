package xyz.anthofoxo.aurora.struct.comp;

import xyz.anthofoxo.aurora.Hash;

public class EditStateComp implements Comp {
	public static int HASH = Hash.fnv1a("EditStateComp");
	public int hash = HASH;
}
