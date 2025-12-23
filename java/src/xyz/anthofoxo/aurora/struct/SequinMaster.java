package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.state.Time;

public class SequinMaster implements ThumperStruct {
	public static final int[] HEADER = { 33, 33, 4, 2 };

	public static class Entry implements ThumperStruct {
		public String lvlName;
		public String gateName;
		public boolean isCheckpoint;
		public String checkpointLeaderLvlName;
		public String restLvlName;
		public byte unknownBool0;
		public byte unknownBool1;
		public int unknown0;
		public byte unknownBool2;
		public boolean playPlus;
	}

	@FixedSize(count = 4)
	public int[] header;
	public int unknown0;
	public int unknown1;
	public int unknown2;
	public Time timeUnit;
	public int unknown3;
	public int unknown4;
	public float unknown5;
	public String skybox;
	public String introLevel;
	public List<Entry> levels;
	public byte footer1;
	public byte footer2;
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
