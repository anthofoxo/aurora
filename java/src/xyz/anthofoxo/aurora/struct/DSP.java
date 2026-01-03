package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class DSP implements ThumperStruct {
	public static int[] header() {
		return new int[] { 4, 4 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	
	// ESPEcho is seen here, we dont know if a list of dsp element appear here or not

}
