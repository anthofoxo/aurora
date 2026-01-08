package xyz.anthofoxo.aurora.struct;

import java.util.ArrayList;
import java.util.List;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
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

	public JsonNode toTcle3(String declarationName) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();

		node.put("obj_type", "SequinGate");
		node.put("obj_name", declarationName);

		node.put("spn_name", entitySpawnerName);

		assert (params.size() == 1);
		ObjectNode param = mapper.createObjectNode();
		param.put("param_path_hash", params.get(0).paramHash + "," + params.get(0).paramIdx);

		node.put("pre_lvl_name", preLevelName);
		node.put("post_lvl_name", postLevelName);
		node.put("restart_lvl_name", restartLevelName);
		node.put("section_type", sectionType);
		node.put("random_type", randomType);

		ArrayNode patternsnode = mapper.createArrayNode();

		for (var boss_pattern : patterns) {
			ObjectNode p = mapper.createObjectNode();

			p.put("lvl_name", boss_pattern.levelName);
			p.put("sentry_type", boss_pattern.sentryType);
			p.put("bucket_num", boss_pattern.bucketNum);

			p.put("node_name_hash", Integer.toHexString(Integer.reverseBytes(boss_pattern.nodeHash)));

			patternsnode.add(p);
		}

		node.set("boss_patterns", patternsnode);

		return node;
	}

	public static SequinGate fromTcle3(JsonNode node) {
		SequinGate gate = new SequinGate();
		gate.entitySpawnerName = node.get("spn_name").asString();
		gate.params = List.of(Tcle3.parseParamPath(node.get("param_path"), node.get("param_path_hash")));

		for (var boss_pattern : node.get("boss_patterns")) {
			var pattern = new SequinGate.BossPattern().withTMLDefaults();

			var nodeName = boss_pattern.get("node_name");
			int hash;

			if (nodeName != null) hash = Hash.fnv1a(nodeName.asString());
			else hash = Integer.reverse(Hash.fnv1a(boss_pattern.get("node_name_hash").asString()));

			pattern.nodeHash = hash;
			pattern.levelName = boss_pattern.get("lvl_name").asString();
			pattern.sentryType = boss_pattern.get("sentry_type").asString();
			pattern.bucketNum = boss_pattern.get("bucket_num").asInt();
			gate.patterns.add(pattern);
		}

		gate.preLevelName = node.get("pre_lvl_name").asString();
		gate.postLevelName = node.get("post_lvl_name").asString();
		gate.restartLevelName = node.get("restart_lvl_name").asString();
		gate.sectionType = node.get("section_type").asString();
		gate.randomType = node.get("random_type").asString();

		return gate;
	}
}
