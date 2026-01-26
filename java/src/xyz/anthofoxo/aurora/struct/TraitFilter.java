package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class TraitFilter {
	public static int[] header() {
		return new int[] { 0x13, 0x15 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public int traitType;
	public boolean traitValue;
	public List<Comp> comps;
}
