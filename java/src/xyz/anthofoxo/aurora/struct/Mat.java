package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class Mat implements ThumperStruct {
	public static int[] header() {
		return new int[] { 0x21, 0x04 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	public String diffuseTexture0;
	public String diffuseTexture1;
	public String cube;
	public String unknownTexture;
	public String blendMode;
	public int unknown4;
	public String cullMode;
	public String depthTest;
	public boolean unknown5;
	public boolean unknown6;
	public String textureFilter;
	/**
	 * This always seems to be this size
	 */
	@FixedSize(count = 22)
	public float[] unknown7;

	public String specular;
	public String xfmOption;

	/**
	 * This always seems to be this size
	 */
	@FixedSize(count = 16)
	public float[] unknown8;
	public byte unknown10;
	public String vig;
	public byte unknown9;
}
