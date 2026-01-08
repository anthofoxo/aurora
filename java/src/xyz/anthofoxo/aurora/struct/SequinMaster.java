package xyz.anthofoxo.aurora.struct;

import java.util.ArrayList;
import java.util.List;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
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

	public JsonNode toTcle3(String declarationName) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();

		node.put("obj_type", "SequinMaster");
		node.put("obj_name", declarationName);
		node.put("skybox_name", skybox);
		node.put("intro_lvl_name", introLevel);
		node.put("isolate_tracks", false);
		node.put("checkpoint_lvl_name", checkpointLvl);

		ArrayNode enteries = mapper.createArrayNode();

		for (var entry : levels) {
			var entryNode = mapper.createObjectNode();
			entryNode.put("isolate", false);
			entryNode.put("lvl_name", entry.lvlName);
			entryNode.put("gate_name", entry.gateName);
			entryNode.put("checkpoint", entry.hasCheckpoint);
			entryNode.put("checkpoint_leader_lvl_name", entry.checkpointLeaderLvlName);
			entryNode.put("rest_lvl_name", entry.restLvlName);
			entryNode.put("play_plus", entry.playPlus);

			enteries.add(entryNode);
		}

		node.set("groupings", enteries);

		return node;
	}

	public static SequinMaster fromTcle3(JsonNode node) {
		SequinMaster master = new SequinMaster();
		master.header = SequinMaster.header();
		master.comps = List.of(new AnimComp(), new EditStateComp());
		master.unknown4 = 0;
		master.unknown5 = 300.0f;
		master.skybox = node.get("skybox_name").asString();
		master.introLevel = node.get("intro_lvl_name").asString();
		master.levels = new ArrayList<>();

		final var tmlMasterIsolate = Tcle3.asBool(node.get("isolate_tracks"));

		for (var grouping : node.get("groupings")) {
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
		master.checkpointLvl = node.get("checkpoint_lvl_name").asString();
		master.pathGameplay = "path.gameplay";

		return master;
	}
}
