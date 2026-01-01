package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class Tex2D implements ThumperStruct {
	public static int[] header() {
		return new int[] { 3, 4 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	public String compression;
	public byte unknown0;
	public byte unknown1;
	public byte unknown2;
	public byte unknown3;
	public byte unknown4;
	public String path;
}
