package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.comp.Comp;

public class Sample implements ThumperStruct {
	public static int[] header() {
		return new int[] { 12, 4 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	public String mode;
	public int unknown0;
	public String path;
	@FixedSize(count = 5)
	public byte[] unknown1;
	public float volume;
	public float pitch;
	public float pan;
	public float offset;
	public String channelGroup;
}