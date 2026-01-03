package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class Vibration implements ThumperStruct {
	public static int[] header() {
		return new int[] { 6, 4 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	public int unknown0;
	public String samplePath;
	public float unknown1;
	public float unknown2;
	public float unknown3;
	public float unknown4;
	public boolean unknown5;
	public int unknown6;
	public int unknown7;
	public String impact;
}
