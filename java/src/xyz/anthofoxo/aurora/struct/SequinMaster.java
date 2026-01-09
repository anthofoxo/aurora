package xyz.anthofoxo.aurora.struct;

import java.util.ArrayList;
import java.util.List;

import tools.jackson.databind.JsonNode;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.AnimComp;
import xyz.anthofoxo.aurora.struct.comp.Comp;
import xyz.anthofoxo.aurora.struct.comp.EditStateComp;
import xyz.anthofoxo.aurora.target.Tcle3;

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

	public static SequinMaster fromTcle3(JsonNode obj) {
		SequinMaster master = new SequinMaster();
		master.header = SequinMaster.header();

		// @formatter:off
		master.comps = List.of(
				new AnimComp(),
				new EditStateComp()
			);
		// @formatter:on

		master.unknown4 = 0;
		master.unknown5 = 300.0f;
		master.skybox = obj.get("skybox_name").asString();
		master.introLevel = obj.get("intro_lvl_name").asString();
		master.levels = new ArrayList<>();

		final var tmlMasterIsolate = Tcle3.asBool(obj.get("isolate_tracks"));

		for (var grouping : obj.get("groupings")) {
			// If track isolation is enabled, only add the isolated tracks to the level.
			// If it's off, isolate_tracks will be False, and so will all instances
			// grouping["isolate"]
			if (Tcle3.asBool(grouping.get("isolate")) == tmlMasterIsolate) {
				var entry = new SequinMaster.Entry();
				entry.lvlName = grouping.get("lvl_name").asString();
				entry.gateName = grouping.get("gate_name").asString();
				entry.hasCheckpoint = Tcle3.asBool(grouping.get("checkpoint"));
				entry.checkpointLeaderLvlName = grouping.get("checkpoint_leader_lvl_name").asString();
				entry.restLvlName = grouping.get("rest_lvl_name").asString();
				entry.unknownBool0 = true;
				entry.unknownBool1 = false;
				entry.unknown0 = 1;
				entry.unknownBool2 = true;
				entry.playPlus = Tcle3.asBool(grouping.get("play_plus"));
				master.levels.add(entry);
			}
		}

		master.footer1 = false;
		master.footer2 = true;
		master.footer3 = 3;
		master.footer4 = 50;
		master.footer5 = 8;
		master.footer6 = 1;
		master.footer7 = 0.6f;
		master.footer8 = 0.5f;
		master.footer9 = 0.5f;
		master.checkpointLvl = obj.get("checkpoint_lvl_name").asString();
		master.pathGameplay = "path.gameplay";

		return master;
	}
}
