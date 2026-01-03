package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class Bender implements ThumperStruct {
	public static int[] header() {
		return new int[] { 0x0D, 0x04 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	public Vec3f unknown0;
	public Vec3f unknown1;
	public Vec3f unknown4;
	public float unknown2;
	public byte unknown3;
}
