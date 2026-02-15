package xyz.anthofoxo.aurora.struct;

import java.util.ArrayList;
import java.util.List;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.yaml.YAMLMapper;
import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.AnimComp;
import xyz.anthofoxo.aurora.struct.comp.Comp;
import xyz.anthofoxo.aurora.struct.comp.EditStateComp;
import xyz.anthofoxo.aurora.struct.sequin.ParamPath;
import xyz.anthofoxo.aurora.target.Tcle3;

public class SequinGate implements ThumperStruct {
	public static class BossPattern implements ThumperStruct {
		public int nodeHash;
		public String levelName;
		public boolean unknown0;
		public String sentryType;
		public int unknown1;
		public int bucketNum;

		public BossPattern withTMLDefaults() {
			unknown0 = true;
			unknown1 = 0;
			return this;
		}
	}

	public static int[] header() {
		return new int[] { 26, 4 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	public String entitySpawnerName;
	public List<ParamPath> params;
	public List<BossPattern> patterns;
	public String preLevelName;
	public String postLevelName;
	public String restartLevelName;

	/**
	 * In all customs and all vanilla levels this is an empty string Level 7 is the
	 * exception, it is seen with this value set to `crakhed_pellet_trans.lvl` in
	 * the gate `crakhed.gate`
	 */
	public String unknown0;
	public String sectionType;
	public float unknown1; // 9
	public String randomType;

	public SequinGate withTMLDefaults() {
		header = SequinGate.header();
		comps = List.of(new EditStateComp());
		patterns = new ArrayList<>();
		unknown0 = "";
		unknown1 = 9;
		return this;
	}

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
			for (var value : params) {
				var n = mapper.createObjectNode();

				var str = Hash.hashes.get(value.paramHash);

				if (str == null) n.put("param", value.paramHash);
				else n.put("param", new String(str));

				n.put("index", value.paramIdx);
				node.add(n);
			}
			root.set("params", node);
		}

		{
			var node = mapper.createArrayNode();
			for (var value : patterns) {
				var n = mapper.createObjectNode();

				var str = Hash.hashes.get(value.nodeHash);
				if (str == null) n.put("nodeHash", value.nodeHash);
				else n.put("nodeHash", new String(str));

				n.put("levelName", value.levelName);
				n.put("unknown0", value.unknown0);
				n.put("sentryType", value.sentryType);
				n.put("unknown1", value.unknown1);
				n.put("bucketNum", value.bucketNum);

				node.add(n);
			}
			root.set("entries", node);
		}

		root.put("entitySpawnerName", entitySpawnerName);
		root.put("preLevelName", preLevelName);
		root.put("postLevelName", postLevelName);
		root.put("restartLevelName", restartLevelName);

		root.put("unknown0", unknown0);
		root.put("sectionType", sectionType);

		root.put("unknown1", unknown1);
		root.put("random_type", randomType);

		return root;
	}

	public static SequinGate fromTcle3(JsonNode obj) {
		SequinGate gate = new SequinGate().withTMLDefaults();
		gate.entitySpawnerName = obj.get("spn_name").asString();
		gate.params = List.of(Tcle3.parseParamPath(obj.get("param_path"), obj.get("param_path_hash")));

		for (var boss_pattern : obj.get("boss_patterns")) {
			var pattern = new SequinGate.BossPattern().withTMLDefaults();

			var nodeName = boss_pattern.get("node_name");
			int hash;

			if (nodeName != null) hash = Hash.fnv1a(nodeName.asString());
			else hash = Tcle3.hexStringToInt(boss_pattern.get("node_name_hash").asString());

			pattern.nodeHash = hash;
			pattern.levelName = boss_pattern.get("lvl_name").asString();
			pattern.sentryType = boss_pattern.get("sentry_type").asString();
			pattern.bucketNum = boss_pattern.get("bucket_num").asInt();
			gate.patterns.add(pattern);
		}

		gate.preLevelName = obj.get("pre_lvl_name").asString();
		gate.postLevelName = obj.get("post_lvl_name").asString();
		gate.restartLevelName = obj.get("restart_lvl_name").asString();
		gate.sectionType = obj.get("section_type").asString();
		gate.randomType = obj.get("random_type").asString();

		return gate;
	}
}
