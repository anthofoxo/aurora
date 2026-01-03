package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class PathDecorator implements ThumperStruct {
	public static int[] header() {
		return new int[] { 0x25, 0x04 };
	}

	public static class CondTestA implements ThumperStruct {
		public GeneralMeshCond cond;

		public boolean unknown2; // 0, 1
		public String pathScaleInterp; // kPathScaleInterpLinear
		public boolean unknown3; // 0, 1
		public boolean unknown4; // 0, 1
		public float unknown5; // 1
		public float unknown6; // 1
		public float unknown7; // 1
		public boolean unknown8; // 1
	}

	public static class GeneralMeshCond implements ThumperStruct {
		public String condition; // start.cond
		public int unknown; // 0
		public String mesh; // chrome_cap.mesh
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps; // EditStateComp
	public List<CondTestA> caps;
	public List<GeneralMeshCond> stencil;

	@FixedSize(count = 9)
	public byte[] bytes; // all 0
}
