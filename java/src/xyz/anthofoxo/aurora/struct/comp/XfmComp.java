package xyz.anthofoxo.aurora.struct.comp;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.Transform;
import xyz.anthofoxo.aurora.struct.trait.TraitConstraint;

/**
 * LE: EB 61 E7 84<br>
 * BE: 84 E7 61 EB
 */
public class XfmComp implements Comp {
	public static final int HASH = Hash.fnv1a("XfmComp");

	public int hash = HASH;
	public int unknown; // Typically 1
	public String xfmName;
	public TraitConstraint constraint;
	public Transform transform;

	public XfmComp() {
	}

	public XfmComp(int unknown, String xfmName, TraitConstraint constraint, Transform transform) {
		this.unknown = unknown;
		this.xfmName = xfmName;
		this.constraint = constraint;
		this.transform = transform;
	}
}
