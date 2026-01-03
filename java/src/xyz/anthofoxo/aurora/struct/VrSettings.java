package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class VrSettings implements ThumperStruct {
	public static int[] header() {
		return new int[] { 0x07, 0x04 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	public String vrCam;
	public String lowSpecScene;
	public String normalScene;
	public String playSpace;
}
