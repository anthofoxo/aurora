package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.comp.Comp;

public class SequinGate implements ThumperStruct {
	public static class ParamPath implements ThumperStruct {
		public int paramHash;
		public int paramIdx; // typically -1

		public ParamPath() {
		}

		public ParamPath(int paramHash, int paramIdx) {
			this.paramHash = paramHash;
			this.paramIdx = paramIdx;
		}
	}

	public static class BossPattern implements ThumperStruct {
		public int nodeHash;
		public String levelName;
		public boolean unknown0; // true
		public String sentryType;
		public int unknown1; // 0
		public int bucketNum;

	}

	public static int[] header() {
		return new int[] { 26, 4 };
	}

	@FixedSize(count = 4)
	public int[] header;
	public List<Comp> comps;
	public String entitySpawnerName;
	public List<ParamPath> params;
	public List<BossPattern> patterns;
	public String preLevelName;
	public String postLevelName;
	public String restartLevelName;
	public int unknown0; // 0
	public String sectionType;
	public int unknown1; // 9
	public String randomType;
}
