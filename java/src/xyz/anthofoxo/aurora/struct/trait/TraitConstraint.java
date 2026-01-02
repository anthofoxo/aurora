package xyz.anthofoxo.aurora.struct.trait;

import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.parse.AuroraWriter;
import xyz.anthofoxo.aurora.struct.ThumperStruct;
import xyz.anthofoxo.aurora.struct.annotation.UnknownNativeName;

@UnknownNativeName
public enum TraitConstraint implements ThumperStruct {
	kConstraintParent, kConstraintBillboard, kConstraintSkybox;

	public static TraitConstraint in(AuroraReader in) {
		return valueOf(in.cstr());
	}

	public static void out(AuroraWriter out, TraitConstraint v) {
		out.str(v.name());
	}
}
