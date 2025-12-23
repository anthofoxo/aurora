package xyz.anthofoxo.aurora.tml;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import xyz.anthofoxo.aurora.Util;
import xyz.anthofoxo.aurora.struct.PrecompiledBin;

public class TMLLevel {
	public static void writeAnimComp(TMLWriter f) {
		f.hash("AnimComp");
		f.i32(1);
		f.f32(0);
		f.str("kTimeBeats");
	}

	public static void writeMaster(TMLWriter f, JsonNode obj) {
		f.i32(33);
		f.i32(33);
		f.i32(4);
		f.i32(2);

		writeAnimComp(f);

		f.hash("EditStateComp");
		f.i32(0);
		f.f32(300.0f);
		f.str(obj.get("skybox_name").asString());
		f.str(obj.get("intro_lvl_name").asString());

		// lvl/.gate groupings
		int isolated = 0;
		for (var grouping : obj.get("groupings")) {
			if (asBool(grouping.get("isolate")) == asBool(obj.get("isolate_tracks"))) isolated++;
		}

		f.i32(isolated);
		for (var grouping : obj.get("groupings")) {
			// If track isolation is enabled, only add the isolated tracks to the level.
			// If it's off, isolate_tracks will be False, and so will all instances
			// grouping["isolate"]
			if (asBool(grouping.get("isolate")) == asBool(obj.get("isolate_tracks"))) {
				f.str(grouping.get("lvl_name").asString());
				f.str(grouping.get("gate_name").asString());
				f.bool(asBool(grouping.get("checkpoint")));
				f.str(grouping.get("checkpoint_leader_lvl_name").asString());
				f.str(grouping.get("rest_lvl_name").asString());
				f.i8arr("01000100000001");
				f.bool(asBool(grouping.get("play_plus")));
			}
		}

		f.bool(false);
		f.bool(true);
		f.i32(3);
		f.i32(50);
		f.i32(8);
		f.i32(1);
		f.f32(0.6F);
		f.f32(0.5F);
		f.f32(0.5F);
		f.str(obj.get("checkpoint_lvl_name").asString());
		f.str("path.gameplay");
	}

	private static boolean isNullOrEmpty(String str) {
		if (str == null) return true;
		if (str.isEmpty()) return true;
		return false;
	}

	private static void writeParamPathU(TMLWriter f, JsonNode param_path, JsonNode param_path_hash) {
		String pp = null;
		String pph = null;

		if (param_path != null) pp = param_path.asString();
		if (param_path_hash != null) pph = param_path_hash.asString();

		writeParamPath(f, pp, pph);
	}

	private static void writeParamPath(TMLWriter f, String param_path, String param_path_hash) {
		f.i32(1);
		String param;
		String param_name;
		String param_idx;

		if (!isNullOrEmpty(param_path)) param = param_path;
		else
			param = param_path_hash;
		// a few specific param paths have a ',' followed by a number. In these special
		// cases, split on ','
		// [0] is the param_name and [1] is the value
		if (param.contains(",")) {
			var _p = param.split(",");
			param_name = _p[0];
			param_idx = _p[1];
		}
		// if the param_path does not have a ',', idx is -1
		else {
			param_name = param;
			param_idx = "-1";
		}
		// depending if the string is plain text or hex-hash, write it to .pc file
		// differently
		if (!isNullOrEmpty(param_path)) f.hash(param_name);
		else
			f.i8arrReverse(param_name);

		f.i32(Integer.parseInt(param_idx));
	}

	private static void Write_Lvl_Header(TMLWriter f) {
		f.i32(51);
		f.i32(33);
		f.i32(4);
		f.i32(2);
	}

	private static void Write_Approach_Anim_Comp(TMLWriter f, JsonNode obj) {
		f.hash("ApproachAnimComp");
		f.i32(1);
		f.f32(0);
		f.str("kTimeBeats");
		f.i32(0);
		f.i32(obj.get("approach_beats").asInt());
	}

	private static List<String> trait_types = Arrays.asList("kTraitInt", "kTraitBool", "kTraitFloat", "kTraitColor",
			"kTraitObj", "kTraitVec3", "kTraitPath", "kTraitEnum", "kTraitAction", "kTraitObjVec", "kTraitString",
			"kTraitCue", "kTraitEvent", "kTraitSym", "kTraitList", "kTraitTraitPath", "kTraitQuat", "kTraitChildLib",
			"kTraitComponent", "kNumTraitTypes");

	private static Set<String> boolIns = new HashSet<>();

	private static void Write_Data_Point_Value(TMLWriter f, String val, String trait_type) {
		if (trait_type.equals("kTraitInt")) f.i32(Integer.parseInt(val));
		else if (trait_type.equals("kTraitBool") || trait_type.equals("kTraitAction")) {
			boolIns.add(val);
			f.bool(toBoolean(val));
		} else if (trait_type.equals("kTraitFloat")) f.f32(Float.parseFloat(val));
		else if (trait_type.equals("kTraitColor")) {
			try {
				Color c = new Color((int) Double.parseDouble(val), true);
				f.f32(c.getRed() / 255f);
				f.f32(c.getGreen() / 255f);
				f.f32(c.getBlue() / 255f);
				f.f32(c.getAlpha() / 255f);
			} catch (NumberFormatException e) {
				f.f32(1);
				f.f32(1);
				f.f32(1);
				f.f32(1);
			}
		}
	}

	private static void Write_Sequencer_Object_v3(TMLWriter f, JsonNode _obj, int beat_cnt) {
		/// header of object
		f.str(_obj.get("obj_name").asString());
		writeParamPathU(f, _obj.get("param_path"), _obj.get("param_path_hash"));
		f.i32(trait_types.indexOf(_obj.get("trait_type").asString()));

		String traittype = _obj.get("trait_type").asString();
		String default_value = _obj.get("default").asString();

		/// data points of object
		//
		// Data points written different depending on STEP
		if (asBool(_obj.get("step")) == true) {
			/// STEP true = value updates every beat, and if no value is set for a beat,
			/// it'll use _obj.default
			f.i32(beat_cnt);
			int indexofwrittenbeat = 0;
			for (int i = 0; i < beat_cnt; i++) {
				f.f32(i);
				if (_obj.get("data_points").size() > indexofwrittenbeat
						&& _obj.get("data_points").get(indexofwrittenbeat).get("beat").asInt() == i) {
					Write_Data_Point_Value(f, _obj.get("data_points").get(indexofwrittenbeat).get("value").asString(),
							traittype);
					f.str(_obj.get("data_points").get(indexofwrittenbeat).get("interp").asString());
					f.str(_obj.get("data_points").get(indexofwrittenbeat).get("ease").asString());
					indexofwrittenbeat++;
				} else {
					Write_Data_Point_Value(f, default_value, traittype);
					f.str("kTraitInterpLinear");
					f.str("kEaseInOut");
				}
			}
		} else {
			/// STEP false = value interpolates between values set on beats. Default
			/// is ignored.
			f.i32(_obj.get("data_points").size());

			for (var dp : _obj.get("data_points")) {
				f.f32(dp.get("beat").asFloat());
				Write_Data_Point_Value(f, dp.get("value").asString(), traittype);
				f.str(dp.get("interp").asString());
				f.str(dp.get("ease").asString());
			}
		}
	}

	private static boolean toBoolean(String s) {
		try {
			return Integer.parseInt(s) == 1;
		} catch (NumberFormatException e) {
		}

		try {
			return Float.parseFloat(s) != 0.0f;
		} catch (NumberFormatException e) {
		}

		return Boolean.parseBoolean(s);
	}

	private static void Write_Sequencer_Objects(TMLWriter f, JsonNode obj) {

		int beat_cnt = 0;
		if (obj.has("beat_cnt")) beat_cnt = obj.get("beat_cnt").asInt();

		var seq_objs = obj.get("seq_objs");
		// write amount of seq_objs (different tracks) to .pc file
		f.i32(seq_objs.size());

		for (var _obj : seq_objs) {
			// if (CurrentLevelProcessing.EditorVersion == 2)
			// Write_Sequencer_Object_v2(f, _obj, beat_cnt);
			// else
			Write_Sequencer_Object_v3(f, _obj, beat_cnt);
			f.i32(0);

			String[] _footer = new String[18];

			if (_obj.get("footer").isArray()) {
				var object = _obj.get("footer");

				for (int i = 0; i < 18; ++i) {
					_footer[i] = object.get(i).asString().trim();
				}
			} else {
				String str = _obj.get("footer").asString();
				str = str.replace("[", "");
				str = str.replace("]", "");
				str = str.replace("'", "");
				_footer = str.split(",");
			}

			f.i32(Integer.parseInt(_footer[0]));
			f.i32(Integer.parseInt(_footer[1]));
			f.i32(Integer.parseInt(_footer[2]));
			f.i32(Integer.parseInt(_footer[3]));
			f.i32(Integer.parseInt(_footer[4]));
			f.str(_footer[5]);
			f.str(_footer[6]);
			f.bool(toBoolean(_footer[7]));
			f.bool(toBoolean(_footer[8]));
			f.i32(Integer.parseInt(_footer[9]));
			f.f32(Float.parseFloat(_footer[10]));
			f.f32(Float.parseFloat(_footer[11]));
			f.f32(Float.parseFloat(_footer[12]));
			f.f32(Float.parseFloat(_footer[13]));
			f.f32(Float.parseFloat(_footer[14]));
			f.bool(toBoolean(_footer[15]));
			f.bool(toBoolean(_footer[16]));
			f.bool(toBoolean(_footer[17]));
		}
	}

	private static void Write_Leaf(TMLWriter f, JsonNode obj) {
		///header
		// honestly don't know what these values do
		f.i32(34);
		f.i32(33);
		f.i32(4);
		f.i32(2);

		///Anim_Comp
		writeAnimComp(f);

		///comp
		f.hash("EditStateComp");
		Write_Sequencer_Objects(f, obj);

		///footer
		int beat_cnt = obj.get("beat_cnt").asInt();
		f.i32(0);
		f.i32(beat_cnt);
		for (int i = 0; i < beat_cnt * 3; i++) f.i32(0);
		f.i32(0);
		f.i32(0);
		f.i32(0);
	}

	private static void Write_Lvl_Comp(TMLWriter f, JsonNode obj) {
		f.hash("EditStateComp");
		Write_Sequencer_Objects(f, obj);

		// .leaf sequence
		f.i32(0);
		f.str("kMovePhaseRepeatChild");
		f.i32(0);
		int last_beat_cnt = 0;
		// iterate over each leaf in the lvl file and write data to file
		for (var leaf : obj.get("leaf_seq")) {
			f.bool(true);
			f.i32(0);
			f.i32(leaf.get("beat_cnt").asInt());
			f.bool(false);
			f.str(leaf.get("leaf_name").asString());
			f.str(leaf.get("main_path").asString());
			f.i32(((ArrayNode) leaf.get("sub_paths")).size());
			for (var sub_path : leaf.get("sub_paths")) {
				f.str(sub_path.asString());
				f.i32(0);
			}
			f.str("kStepGameplay");
			f.i32(last_beat_cnt);
			f.vec3((ArrayNode) leaf.get("pos"));
			f.vec3((ArrayNode) leaf.get("rot_x"));
			f.vec3((ArrayNode) leaf.get("rot_y"));
			f.vec3((ArrayNode) leaf.get("rot_z"));
			f.vec3((ArrayNode) leaf.get("scale"));
			f.i8arr("0000");
			last_beat_cnt = leaf.get("beat_cnt").asInt();
		}

		f.bool(false);
		// write loops
		f.i32(obj.get("loops").size());
		for (var loop : obj.get("loops")) {
			f.str(loop.get("samp_name").asString());
			f.i32(loop.get("beats_per_loop").asInt());
			f.i32(0);
		}
	}

	private static void Write_Lvl_Footer(TMLWriter f, JsonNode obj) {
		f.bool(false);
		f.f32(obj.get("volume").asFloat());
		f.i32(0);
		f.i32(0);
		f.str("kNumTraitTypes");
		f.bool(asBool(obj.get("input_allowed")));
		f.str(obj.get("tutorial_type").asString());
		f.vec3((ArrayNode) obj.get("start_angle_fracs"));
	}

	public static void Write_Xfm_Header(TMLWriter f) {
		f.i32(4);
		f.i32(4);
		f.i32(1);
	}

	private static void Write_Xfm_Comp(TMLWriter f, JsonNode obj) {
		f.hash("XfmComp");
		f.i32(1);
		f.str(obj.get("xfm_name").asString());
		f.str(obj.get("constraint").asString());
		f.vec3((ArrayNode) obj.get("pos"));
		f.vec3((ArrayNode) obj.get("rot_x"));
		f.vec3((ArrayNode) obj.get("rot_y"));
		f.vec3((ArrayNode) obj.get("rot_z"));
		f.vec3((ArrayNode) obj.get("scale"));
	}

	public static void writeSpn(TMLWriter f, JsonNode obj) {
		f.i32(1);
		f.i32(4);
		f.i32(2);

		///comp
		f.hash("EditStateComp");

		/// xfm comp
		Write_Xfm_Comp(f, obj);

		///footer
		f.i32(0);
		f.str(obj.get("objlib_path").asString());
		f.str(obj.get("bucket").asString());
	}

	public static void Write_Samp(TMLWriter f, JsonNode obj) {
		///header
		f.i32(12);
		f.i32(4);
		f.i32(1);

		///comp
		f.hash("EditStateComp");
		f.str(obj.get("mode").asString());
		f.i32(0);
		f.str(obj.get("path").asString());
		f.i8arr("0000000000");
		f.f32(obj.get("volume").asFloat());
		f.f32(obj.get("pitch").asFloat());
		f.f32(obj.get("pan").asFloat());
		f.f32(obj.get("offset").asFloat());
		f.str(obj.get("channel_group").asString());
	}

	public static void writeGate(TMLWriter f, JsonNode obj) {
		f.i32(26);
		f.i32(4);
		f.i32(1);

		f.hash("EditStateComp");
		f.str(obj.get("spn_name").asString());
		writeParamPathU(f, obj.get("param_path"), obj.get("param_path_hash"));

		f.i32(((ArrayNode) obj.get("boss_patterns")).size());
		for (var boss_pattern : obj.get("boss_patterns")) {
			var nodeName = boss_pattern.get("node_name");
			if (nodeName != null) f.hash(nodeName.asString());
			else
				f.i8arrReverse(boss_pattern.get("node_name_hash").asString());
			f.str(boss_pattern.get("lvl_name").asString());
			f.bool(true);
			f.str(boss_pattern.get("sentry_type").asString());
			f.i8arr("00000000");
			f.i32(boss_pattern.get("bucket_num").asInt());
		}

		f.str(obj.get("pre_lvl_name").asString());
		f.str(obj.get("post_lvl_name").asString());
		f.str(obj.get("restart_lvl_name").asString());
		f.i32(0);
		f.str(obj.get("section_type").asString());
		f.f32(9);
		f.str(obj.get("random_type").asString());
	}

	static List<String> obj_types = Arrays.asList("SequinLeaf", "SequinLevel", "SequinGate", "SequinMaster",
			"EntitySpawner", "Sample", "Xfmer");

	static List<String> file_types = Arrays.asList(".gate", ".leaf", ".lvl", ".master", ".xfm", ".config");

	public static String getPathExtensionWithDot(Path path) {

		// Get the filename part to handle directory separators correctly

		String fileName = path.getFileName().toString().toLowerCase();

		int lastDotIndex = fileName.lastIndexOf('.');

		// Check for edge cases:
		// 1. No dot found, or the dot is the first character (hidden file without
		// extension)
		if (lastDotIndex == -1 || lastDotIndex == 0) {
			return "";
		}

		// Return the substring starting from the last dot
		return fileName.substring(lastDotIndex);
	}

	private static boolean asBool(JsonNode node) {
		if (node == null || node.isNull()) return false;
		if (node.isBoolean()) return node.asBoolean();
		return Boolean.parseBoolean(node.asString());
	}

	private static List<String> file_special = Arrays.asList(".spn", ".samp");

	public static void test() throws IOException {

		int loadedLevelIndex = 0;
		JsonMapper mapper = JsonMapper.builder().configure(JsonReadFeature.ALLOW_SINGLE_QUOTES, true).build();

		JsonNode level_config = null;
		List<JsonNode> objs = new ArrayList<>();

		objs.add(mapper.readTree(Util.getResourceBytes("leaf_pyramid_outro.txt")));

		var files = Files.walk(Path.of("C:\\Users\\antho\\Downloads\\Thumper.Mod.Loader.2.0\\levels\\Trial of 0 Gates"))
				.collect(Collectors.toList());

		for (var path : files) {

			if (file_types.contains(getPathExtensionWithDot(path))) {
				// read file and store JSON in dynamic object
				var new_objs = mapper.readTree(path);
				objs.add(new_objs);

			}
			// these file types require different processing to get the data
			else if (file_special.contains(getPathExtensionWithDot(path))) {

				var new_objs = mapper.readTree(path);

				// spn_ and samp_ files contain multiple entries, inside the "multi":[] list
				for (var _v : new_objs.get("items")) {
					objs.add(_v);

				}
			} else if (getPathExtensionWithDot(path).equals(".tcl")) {
				level_config = mapper.readTree(path);
			}
		}

		TMLWriter writer = new TMLWriter();
		writer.i8arr(PrecompiledBin.getHeaderBin());
		writer.str(String.format("levels/custom/level%s.objlib", 1));
		// Write_String(f,
		// $"levels/custom/level{LoadedLevels.IndexOf(Level)+1}.objlib");
		writer.i8arr(PrecompiledBin.getObjList1Bin());

		writer.i32(63 + objs.size());

		writer.i8arr(PrecompiledBin.getObjList2Bin());

		for (var obj : objs) {
			if (obj_types.contains(obj.get("obj_type").asString())) {
				if (!obj.get("obj_type").asString().equals("Xfmer")) {
					writer.hash(obj.get("obj_type").asString());
					writer.str(obj.get("obj_name").asString());
				} else {
					writer.hash(obj.get("obj_type").asString());
					writer.str(String.format("levels/custom/level%d.xfm", loadedLevelIndex + 1));
				}
			}
		}

		writer.i8arr(Util.getResourceBytes("obj_def_customlevel.objlib"));

		for (var obj : objs) {
			if (obj.get("obj_type").asString().equals("SequinLeaf")) Write_Leaf(writer, obj);
			else if (obj.get("obj_type").asString().equals("SequinLevel")) {
				Write_Lvl_Header(writer);
				Write_Approach_Anim_Comp(writer, obj);
				Write_Lvl_Comp(writer, obj);
				Write_Lvl_Footer(writer, obj);
			} else if (obj.get("obj_type").asString().equals("SequinGate")) writeGate(writer, obj);
			else if (obj.get("obj_type").asString().equals("SequinMaster")) writeMaster(writer, obj);
			else if (obj.get("obj_type").asString().equals("EntitySpawner")) writeSpn(writer, obj);
			else if (obj.get("obj_type").asString().equals("Sample")) Write_Samp(writer, obj);
			else if (obj.get("obj_type").asString().equals("Xfmer")) {
				Write_Xfm_Header(writer);
				Write_Xfm_Comp(writer, obj);
			}
		}

		writer.i8arr(Util.getResourceBytes("footer_1.objlib"));
		writer.f32(level_config.get("bpm").asFloat());
		writer.i8arr(Util.getResourceBytes("footer_2.objlib"));

		Files.write(Path.of("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Thumper\\cache\\73bb8d2d.pc"),
				writer.toBytes());

		System.out.println(boolIns);
	}
}
