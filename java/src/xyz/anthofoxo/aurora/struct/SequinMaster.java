package xyz.anthofoxo.aurora.struct;

import java.util.ArrayList;
import java.util.List;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.yaml.YAMLMapper;
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

		public ObjectNode toAur() {
			YAMLMapper mapper = new YAMLMapper();
			var root = mapper.createObjectNode();
			if (!lvlName.isEmpty()) root.put("level_name", lvlName);
			if (!gateName.isEmpty()) root.put("gate_name", gateName);
			if (!hasCheckpoint) root.put("checkpoint", hasCheckpoint);
			if (!checkpointLeaderLvlName.isEmpty()) root.put("checkpoint_leader_level", checkpointLeaderLvlName);
			if (!restLvlName.isEmpty()) root.put("rest_level", restLvlName);
			if (!unknownBool0) root.put("_unknownBool0", unknownBool0);
			if (unknownBool1) root.put("_unknownBool1", unknownBool1);
			if (unknown0 != 1) root.put("_unknown0", unknown0);
			if (!unknownBool2) root.put("_unknownBool2", unknownBool2);
			if (!playPlus) root.put("play_plus", playPlus);
			return root;

		}

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

	public ObjectNode toAur() {
		YAMLMapper mapper = new YAMLMapper();
		var root = mapper.createObjectNode();

		{
			var node = mapper.createArrayNode();
			for (int value : header) node.add(value);
			root.set("header", node);
		}

		{
			var node = mapper.createObjectNode();

			for (var comp : comps) {
				var compNode = mapper.createObjectNode();

				if (comp instanceof EditStateComp) {
				} else if (comp instanceof AnimComp c) {
					compNode.put("unknown0", c.unknown0);
					compNode.put("unknown1", c.unknown1);
					compNode.put("unit", c.timeUnit.name());
				} else {
					compNode.put("warning", "comp not extracted");
				}

				node.set(comp.getClass().getSimpleName(), compNode);
			}

			root.set("components", node);

		}

		{
			var node = mapper.createArrayNode();
			for (var entry : levels) node.add(entry.toAur());
			root.set("entries", node);
		}

		root.put("unknown4", unknown4);
		root.put("unknown5", unknown5);
		root.put("skybox", skybox);
		if (!introLevel.isEmpty()) root.put("intro_level", introLevel);
		root.put("footer1", footer1);
		root.put("footer2", footer2);
		root.put("footer3", footer3);
		root.put("footer4", footer4);
		root.put("footer5", footer5);
		root.put("footer6", footer6);
		root.put("footer7", footer7);
		root.put("footer8", footer8);
		root.put("footer9", footer9);
		if (!checkpointLvl.isEmpty()) root.put("checkpoint_level", checkpointLvl);
		if (!pathGameplay.equals("path.gameplay")) root.put("path_gameplay", pathGameplay);

		return root;
	}

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
