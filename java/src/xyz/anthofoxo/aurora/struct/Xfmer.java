package xyz.anthofoxo.aurora.struct;

import java.util.List;

public class Xfmer implements ThumperStruct {
	public static int[] header() {
		return new int[] { 4, 4 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
}
