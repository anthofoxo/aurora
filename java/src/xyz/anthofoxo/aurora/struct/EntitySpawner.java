package xyz.anthofoxo.aurora.struct;

import java.util.List;

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
