package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class SequinMaster implements ThumperStruct {
	public static int[] header() {
		return new int[] { 33, 33, 4 };
	}

	public static class Entry implements ThumperStruct {
		public String lvlName;
		public String gateName;
		public boolean hasCheckpoint;
		public String checkpointLeaderLvlName;
		public String restLvlName;
		public boolean unknownBool0;
		public boolean unknownBool1;
		public int unknown0;
		public boolean unknownBool2;
		public boolean playPlus;

		@Override
		public String toString() {
			if (gateName.isEmpty()) return lvlName;
			return gateName;
		}
	}

	@FixedSize(count = 3)
	public int[] header;
	public List<Comp> comps;
	public int unknown4;
	public float unknown5;
	public String skybox;
	public String introLevel;
	public List<Entry> levels;
	public boolean footer1;
	public boolean footer2;
	public int footer3;
	public int footer4;
	public int footer5;
	public int footer6;
	public float footer7;
	public float footer8;
	public float footer9;
	public String checkpointLvl;
	public String pathGameplay;
}
