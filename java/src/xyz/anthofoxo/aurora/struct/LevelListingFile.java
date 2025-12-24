package xyz.anthofoxo.aurora.struct;

import java.util.List;

public class LevelListingFile implements ThumperStruct {
	public static class Entry implements ThumperStruct {
		public String key;
		public int unknown0;
		public String path;
		public String unlocks;
		public boolean defaultLocked;
		public boolean unknown1;
		
		/**
		 * When set to true, upon level completion, the credit sequence is triggered
		 */
		public boolean triggersCredits;
		
		public int colorIndex0;
		public int colorIndex1;
	}

	public int fileType = 16;
	public List<Entry> enteries;
}
