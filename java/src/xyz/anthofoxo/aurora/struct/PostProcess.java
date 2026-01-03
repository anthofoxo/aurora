package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class PostProcess implements ThumperStruct {
	public static int[] header() {
		return new int[] { 0x02, 0x04 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
}
