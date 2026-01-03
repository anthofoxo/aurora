package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class Env implements ThumperStruct {
	public static int[] header() {
		return new int[] { 0x09, 0x04 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;

	public Vec3f unknown0;
	public float unknown1;

	public List<String> lit;

	public Vec3f unknown2;
	public Vec2f unknown3;
}
