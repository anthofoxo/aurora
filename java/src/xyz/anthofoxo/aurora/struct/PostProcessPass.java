package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class PostProcessPass implements ThumperStruct {
	public static int[] header() {
		return new int[] { 0x05, 0x04 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	public List<String> pps;
	public boolean unknown0;
	public boolean unknown1;
}
