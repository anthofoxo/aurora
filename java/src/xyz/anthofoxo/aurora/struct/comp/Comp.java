package xyz.anthofoxo.aurora.struct.comp;

import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.struct.ThumperStruct;

/**
 * This is the base class for thumper components, many subtypes of comp are only
 * seen in specific definitions
 * 
 * If a new comp definition is fully defined then a check for it must be added
 * below
 */
public interface Comp extends ThumperStruct {

	/**
	 * Used when reading from an AuroraReader
	 * 
	 * Since we can't know at load time what components are in what order, we have
	 * to read the heading hash and parse accordingly
	 * 
	 * When this is invoked, is is guarenteed to be loading a class of *exactly*
	 * Comp meaning the runtime type was erased
	 */
	public static Comp in(AuroraReader in) {
		// Read the hash header
		var currentPos = in.position();
		int hash = in.i32();

		// Rewind the cursor to before reading the hash to allow the reflection to fully
		// fill out the struct
		in.seek(currentPos);

		// Check against our known comps, if we encounter a comp type we havn't seen
		// then we can no longer safely read
		if (hash == AnimComp.HASH) return in.obj(AnimComp.class);
		else if (hash == ApproachAnimComp.HASH) return in.obj(ApproachAnimComp.class);
		else if (hash == DrawComp.HASH) return in.obj(DrawComp.class);
		else if (hash == EditStateComp.HASH) return in.obj(EditStateComp.class);
		else if (hash == XfmComp.HASH) return in.obj(XfmComp.class);
		else if (hash == PollComp.HASH) return in.obj(PollComp.class);
		else if (hash == PPVignette.HASH) return in.obj(PPVignette.class);
		else if (hash == DSPEcho.HASH) return in.obj(DSPEcho.class);
		else throw new IllegalStateException("Unknown comp type: " + Integer.toHexString(hash) + " at offset 0x"
				+ Integer.toHexString(in.position()));
	}

}
