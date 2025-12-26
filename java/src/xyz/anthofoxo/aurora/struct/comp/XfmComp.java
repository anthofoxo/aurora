package xyz.anthofoxo.aurora.struct.comp;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.Transform;

public class XfmComp implements Comp {
	public static final int HASH = Hash.fnv1a("XfmComp");

	public int hash = HASH;
	public int unknown = 1;
	public String xfmName;
	public String constraint;
	public Transform transform;

	public XfmComp() {
	}

	public XfmComp(String xfmName, String constraint, Transform transform) {
		this.xfmName = xfmName;
		this.constraint = constraint;
		this.transform = transform;
	}
}
