package xyz.anthofoxo.aurora.struct.experimental;

import java.util.List;

import xyz.anthofoxo.aurora.struct.ThumperStruct;
import xyz.anthofoxo.aurora.struct.Vec3f;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class Cam implements ThumperStruct {
	public static int[] header() {
		return new int[] { 6, 4 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	public Vec3f extra;

	public Cam() {
	}

	public Cam(List<Comp> comps, Vec3f extra) {
		this.header = header();
		this.comps = comps;
		this.extra = extra;
	}

}
