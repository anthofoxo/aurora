package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;
import xyz.anthofoxo.aurora.struct.sequin.Trait;

public class SequinLeaf implements ThumperStruct {
	public static class Loop implements ThumperStruct {
		public String sampleName;
		public int beatsPerLoop;
		public int unknown;
	}

	public static int[] header() {
		return new int[] { 34, 33, 4 };
	}

	@FixedSize(count = 3)
	public int[] header;
	public List<Comp> comps;
	public List<Trait> objects;
	public int unknown0;
	public List<Vec3f> unknownBeatFooter;
	public Vec3f finalFooter;
}
