package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.SequinLeaf.Trait;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class SequinPulse implements ThumperStruct {
	public static int[] header() {
		return new int[] { 0x0F, 0x21, 0x04 };
	}

	@FixedSize(count = 3)
	public int[] header;
	public List<Comp> comps;
	public List<Trait> traits;

	public Vec3f unknown0;

	@FixedSize(count = 5)
	public String[] unknown1;

	public boolean unknown2;
	public boolean unknown3;

}
