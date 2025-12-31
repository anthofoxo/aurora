package xyz.anthofoxo.aurora.struct.comp;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.annotation.KnownNativeName;

@KnownNativeName
public class ApproachAnimComp implements Comp {
	public static final int HASH = Hash.fnv1a("ApproachAnimComp");

	public int hash = HASH;
	public int unknown0 = 1;
	public float unknown1 = 0;
	public String timeBeats = "kTimeBeats";
	public int unknown2 = 0;
	public int approachBeats;

	public ApproachAnimComp withApproachBeats(int approachBeats) {
		this.approachBeats = approachBeats;
		return this;
	}
}
