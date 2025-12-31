package xyz.anthofoxo.aurora.struct.comp;

import java.util.List;

import xyz.anthofoxo.aurora.struct.ThumperStruct;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.annotation.UnknownNativeName;

@UnknownNativeName
public class _Mesh implements ThumperStruct {
	public static int[] header() {
		return new int[] { 15, 4 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	public String material;
	public int unknown0;
	public boolean unknown1;
	public String mesh;
	@FixedSize(count = 17)
	public byte[] hashdata;
}
