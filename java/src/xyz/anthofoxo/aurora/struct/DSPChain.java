package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class DSPChain implements ThumperStruct {
	public static int[] header() {
		return new int[] { 1, 4 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	public boolean unknown;
	public String channel;
	public int unknown1;
	public String DSPs;
}
