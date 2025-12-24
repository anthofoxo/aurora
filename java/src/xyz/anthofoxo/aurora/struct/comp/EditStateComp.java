package xyz.anthofoxo.aurora.struct.comp;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.Comp;

public class EditStateComp implements Comp {
	public int hash = Hash.fnv1a("EditStateComp");
}
