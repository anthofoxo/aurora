package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class ChannelGroup implements ThumperStruct {
	public static int[] header() {
		return new int[] { 4, 4 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	public float unknown;
	public String channel;
	public float unknown1;
	public boolean unknown2;
}
