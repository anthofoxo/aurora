package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class EntitySpawner implements ThumperStruct {
	public static int[] header() {
		return new int[] { 1, 4 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	public int unknown;
	public String objlibPath;
	public String bucket;
}
