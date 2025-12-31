package xyz.anthofoxo.aurora.struct.trait;

import xyz.anthofoxo.aurora.struct.annotation.UnknownNativeName;

@UnknownNativeName
public class TraitConstraint {
	public static final TraitConstraint Parent = new TraitConstraint("kConstraintParent");
	public static final TraitConstraint Billboard = new TraitConstraint("kConstraintBillboard");
	public static final TraitConstraint Skybox = new TraitConstraint("kConstraintSkybox");

	public String value;

	public TraitConstraint() {
	}

	public TraitConstraint(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
