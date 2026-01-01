package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class Mat {
	public static int[] header() {
		return new int[] { 0x21, 0x04 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	public int unknown0;
	public int unknown1;
	public String unknown2; // aurora2.cube
	public int unknown3;
	public String blendMode; //kSourceSubtractive
	public int unknown4;
	public String cullMode;
	public String depthTest;
	public boolean unknown5;
	public boolean unknown6;
	public String textureFilter;
	@FixedSize(count = 0x5C)
	public byte[] unknown7;
	public String textureBindPoint;
	@FixedSize(count = 0x46)
	public byte[] unknown8;
}
