package xyz.anthofoxo.aurora.struct;

import java.time.Instant;
import java.util.List;

public class SaveFile implements ThumperStruct {
	public static class RankEntry implements ThumperStruct {
		public String entry;
		public int unknown;

		@Override
		public String toString() {
			return entry;
		}
	}

	public static class LevelEntry implements ThumperStruct {
		public String key;
		public String playRank;
		public int playScore;
		public String playRankDup;
		public boolean unknown0;
		public long timestamp;
		public int plusScore;
		public String plusRank;
		public String plusRankDup;
		public int unknown1;
		public List<RankEntry> playRanks;
		public int unknown2;
		public List<RankEntry> plusRanks;
	}

	public int fileHeader;

	/**
	 * this field is the byte length of the *entire* file
	 */
	public int byteCount;

	public Instant timestamp;
	public List<LevelEntry> enteries;

	// We dont have the exact struct for the rest of the file
	// simply copy these back into the output
	public byte[] remaining;

	public static SaveFile in(AuroraReader in) {
		var instance = new SaveFile();
		instance.fileHeader = in.i32();
		instance.byteCount = in.i32();
		instance.timestamp = Instant.ofEpochSecond(in.i64());
		instance.enteries = in.objlist(LevelEntry.class);
		instance.remaining = in.i8remaining();
		return instance;
	}

	public static void out(AuroraWriter out, SaveFile instance) {
		// First write to a dummy object so we can calculate the byte size
		AuroraWriter temp = new AuroraWriter();
		temp.i32(instance.fileHeader);
		temp.i32(instance.byteCount);
		temp.i64(instance.timestamp.getEpochSecond());
		temp.objlist(instance.enteries);
		temp.i8arr(instance.remaining);
		// Our new byte size is now the size of the written object
		instance.byteCount = temp.position();

		// Dump the updated object with the new byte count
		out.i32(instance.fileHeader);
		out.i32(instance.byteCount);
		out.i64(instance.timestamp.getEpochSecond());
		out.objlist(instance.enteries);
		out.i8arr(instance.remaining);
	}
}
