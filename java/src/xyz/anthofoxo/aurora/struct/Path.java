package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class Path implements ThumperStruct {
	public static int[] header() {
		return new int[] { 0x29, 0x04 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	public Vec3f unknown0;
	public Vec3f unknown1;
	public int unknown2;
	public String meshObject;
	public boolean unknown3;
	public String pathInterp;
	@FixedSize(count = 6)
	public byte[] unknown4;
	public List<String> decorators;
	public boolean unknown5;
}
