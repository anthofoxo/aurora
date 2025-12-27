package xyz.anthofoxo.aurora.struct.comp;

import xyz.anthofoxo.aurora.Hash;

public class EditStateComp implements Comp {
	/**
	 * LE: 12 FB 8E 3C<br>
	 * BE: 3C 8E FB 12
	 */
	public static int HASH = Hash.fnv1a("EditStateComp");
	public int hash = HASH;
}
