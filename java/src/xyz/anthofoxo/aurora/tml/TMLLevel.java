package xyz.anthofoxo.aurora.tml;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import tools.jackson.core.JacksonException;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.Util;
import xyz.anthofoxo.aurora.struct.AuroraReader;
import xyz.anthofoxo.aurora.struct.AuroraWriter;
import xyz.anthofoxo.aurora.struct.LevelListingFile;
import xyz.anthofoxo.aurora.struct.LocalizationFile;
import xyz.anthofoxo.aurora.struct.PrecompiledBin;
import xyz.anthofoxo.aurora.struct.Sample;
import xyz.anthofoxo.aurora.struct.SectionFile;
import xyz.anthofoxo.aurora.struct.SequinMaster;
import xyz.anthofoxo.aurora.struct.ThumperStruct;
import xyz.anthofoxo.aurora.struct.comp.AnimComp;
import xyz.anthofoxo.aurora.struct.comp.EditStateComp;

public class TMLLevel {
	public static void writeAnimComp(AuroraWriter f) {
		f.hash("AnimComp");
		f.i32(1);
		f.f32(0);
		f.str("kTimeBeats");
	}

	public static SequinMaster toSequinMaster(JsonNode obj) {
		SequinMaster master = new SequinMaster();
		master.header = SequinMaster.HEADER.clone();

		// @formatter:off
		master.comps = Arrays.asList(
				new AnimComp(),
				new EditStateComp()
			);
		// @formatter:on

		master.unknown4 = 0;
		master.unknown5 = 300.0f;
		master.skybox = obj.get("skybox_name").asString();
		master.introLevel = obj.get("intro_lvl_name").asString();
		master.levels = new ArrayList<>();

		final var tmlMasterIsolate = asBool(obj.get("isolate_tracks"));

		for (var grouping : obj.get("groupings")) {
			// If track isolation is enabled, only add the isolated tracks to the level.
			// If it's off, isolate_tracks will be False, and so will all instances
			// grouping["isolate"]
			if (asBool(grouping.get("isolate")) == tmlMasterIsolate) {
				var entry = new SequinMaster.Entry();
				entry.lvlName = grouping.get("lvl_name").asString();
				entry.gateName = grouping.get("gate_name").asString();
				entry.hasCheckpoint = asBool(grouping.get("checkpoint"));
				entry.checkpointLeaderLvlName = grouping.get("checkpoint_leader_lvl_name").asString();
				entry.restLvlName = grouping.get("rest_lvl_name").asString();
				entry.unknownBool0 = true;
				entry.unknownBool1 = false;
				entry.unknown0 = 1;
				entry.unknownBool2 = true;
				entry.playPlus = asBool(grouping.get("play_plus"));
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

	private static boolean isNullOrEmpty(String str) {
		if (str == null) return true;
		if (str.isEmpty()) return true;
		return false;
	}

	private static void writeParamPathU(AuroraWriter f, JsonNode param_path, JsonNode param_path_hash) {
		String pp = null;
		String pph = null;

		if (param_path != null) pp = param_path.asString();
		if (param_path_hash != null) pph = param_path_hash.asString();

		writeParamPath(f, pp, pph);
	}

	private static void writeParamPath(AuroraWriter f, String param_path, String param_path_hash) {
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

	private static void Write_Lvl_Header(AuroraWriter f) {
		f.i32(51);
		f.i32(33);
		f.i32(4);
		f.i32(2);
	}

	private static void Write_Approach_Anim_Comp(AuroraWriter f, JsonNode obj) {
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

	private static void Write_Data_Point_Value(AuroraWriter f, String val, String trait_type) {
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

	private static void Write_Sequencer_Object_v3(AuroraWriter f, JsonNode _obj, int beat_cnt) {
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

	private static void Write_Sequencer_Objects(AuroraWriter f, JsonNode obj) {

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

	private static void Write_Leaf(AuroraWriter f, JsonNode obj) {
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

	private static void Write_Lvl_Comp(AuroraWriter f, JsonNode obj) {
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

	private static void Write_Lvl_Footer(AuroraWriter f, JsonNode obj) {
		f.bool(false);
		f.f32(obj.get("volume").asFloat());
		f.i32(0);
		f.i32(0);
		f.str("kNumTraitTypes");
		f.bool(asBool(obj.get("input_allowed")));
		f.str(obj.get("tutorial_type").asString());
		f.vec3((ArrayNode) obj.get("start_angle_fracs"));
	}

	public static void Write_Xfm_Header(AuroraWriter f) {
		f.i32(4);
		f.i32(4);
		f.i32(1);
	}

	private static void Write_Xfm_Comp(AuroraWriter f, JsonNode obj) {
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

	public static void writeSpn(AuroraWriter f, JsonNode obj) {
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

	public static Sample toSample(JsonNode obj) {
		Sample sample = new Sample();
		sample.header = Sample.HEADER;
		sample.comps = Arrays.asList(new EditStateComp());
		sample.mode = obj.get("mode").asString();
		sample.unknown0 = 0;
		sample.path = obj.get("path").asString();
		sample.unknown1 = new byte[] { 0, 0, 0, 0, 0 };
		sample.volume = obj.get("volume").asFloat();
		sample.pitch = obj.get("pitch").asFloat();
		sample.pan = obj.get("pan").asFloat();
		sample.offset = obj.get("offset").asFloat();
		sample.channelGroup = obj.get("channel_group").asString();
		return sample;
	}

	public static void writeGate(AuroraWriter f, JsonNode obj) {
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

	private static class GeneratedAssets {
		public HashMap<String, byte[]> pcFiles = new HashMap<>();
		public String levelName;
		public byte[] objlib;
		public byte[] sec;
		public String localizationKey;
		public String localizationValue;
	}

	static JsonMapper mapper = JsonMapper.builder().configure(JsonReadFeature.ALLOW_SINGLE_QUOTES, true).build();

	public static GeneratedAssets build_level(Path levelPath) throws JacksonException, IOException {
		var files = Files.walk(levelPath).collect(Collectors.toList());

		// Static storage
		GeneratedAssets assets = new GeneratedAssets();
		JsonNode level_config = null;
		List<JsonNode> objs = new ArrayList<>();
		objs.add(mapper.readTree(Util.getResourceBytes("leaf_pyramid_outro.txt")));

		// Iterate over all files, put them into either the pc file list or make a
		// jsonobject
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
			} else if (getPathExtensionWithDot(path).equals(".pc")) {
				assets.pcFiles.put(path.getFileName().toString(), Files.readAllBytes(path));
			}
		}

		if (level_config == null) {
			throw new IllegalStateException("TCLE 2.x Levels are not supported, Covert them to 3.x");
		}

		assets.levelName = level_config.get("level_name").asString();

		AuroraWriter writer = new AuroraWriter();
		writer.i8arr(PrecompiledBin.getHeaderBin());
		writer.str(String.format("levels/custom/%s.objlib", level_config.get("level_name").asString()));
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
					writer.str(String.format("levels/custom/%s.xfm", assets.levelName));
				}
			}
		}

		writer.i8arr(Util.getResourceBytes("obj_def_customlevel.objlib"));

		for (var obj : objs) {
			final String objType = obj.get("obj_type").asString();

			if (objType.equals("SequinLeaf")) Write_Leaf(writer, obj);
			else if (objType.equals("SequinLevel")) {
				Write_Lvl_Header(writer);
				Write_Approach_Anim_Comp(writer, obj);
				Write_Lvl_Comp(writer, obj);
				Write_Lvl_Footer(writer, obj);
			} else if (objType.equals("SequinGate")) writeGate(writer, obj);
			else if (objType.equals("SequinMaster")) writer.obj(toSequinMaster(obj));
			else if (objType.equals("EntitySpawner")) writeSpn(writer, obj);
			else if (objType.equals("Sample")) writer.obj(toSample(obj));
			else if (objType.equals("Xfmer")) {
				Write_Xfm_Header(writer);
				Write_Xfm_Comp(writer, obj);
			}
		}

		writer.i8arr(Util.getResourceBytes("footer_1.objlib"));
		writer.f32(level_config.get("bpm").asFloat());
		writer.i8arr(Util.getResourceBytes("footer_2.objlib"));

		AuroraWriter sec = new AuroraWriter();

		sec.obj(SectionFile.fromTML(level_config));

		assets.objlib = writer.getBytes();
		assets.sec = sec.getBytes();
		assets.localizationKey = String.format("custom.%s", level_config.get("level_name").asString());
		assets.localizationValue = level_config.get("level_name").asString();
		return assets;
	}

	public static void test() throws IOException {
		// Find all customs listed
		List<Path> customs;

		try (var stream = Files.list(Path.of("aurora_mods"))) {
			customs = stream.collect(Collectors.toList());
		}

		List<GeneratedAssets> assets = new ArrayList<>();

		for (Path path : customs) {
			if (path.getFileName().toString().endsWith(".zip")) continue; // Ignore zips
			if (Files.isRegularFile(path)) continue; // Sinular files arent supported yet

			boolean hasTcl = false;
			boolean hasObjlib = false;
			boolean hasTcl2 = false;

			try (var stream = Files.list(path)) {
				List<Path> files = stream.collect(Collectors.toList());

				for (var file : files) {
					String fname = file.getFileName().toString().toLowerCase();

					if (fname.endsWith(".tcl")) hasTcl = true;
					if (fname.endsWith(".objlib")) hasObjlib = true;
					if (fname.startsWith("config_") && fname.endsWith(".txt")) hasTcl2 = true;
				}
			}

			if (hasTcl2) {
				System.err
						.println(path.getFileName() + " is a TCL2 level, these are not supported, update to TCLE 3.x");
				continue;
			}

			if (hasTcl && hasObjlib) {
				System.err.println(path.getFileName() + " is a TCLE Compiled Level, these are not supported yet");
				continue;
			}

			if (!hasTcl) continue; // Not supported
			if (hasObjlib) continue; // Not supported

			assets.add(build_level(path));
		}

		// write out the level files
		for (var asset : assets) {
			// write objlib
			int target = Hash.fnv1a(String.format("Alevels/custom/%s.objlib", asset.levelName));
			Files.write(Path.of("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Thumper\\cache\\"
					+ Integer.toHexString(target) + ".pc"), asset.objlib);

			// write sec
			target = Hash.fnv1a(String.format("Alevels/custom/%s.sec", asset.levelName));
			Files.write(Path.of("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Thumper\\cache\\"
					+ Integer.toHexString(target) + ".pc"), asset.sec);

			// write pc files
			for (var pc : asset.pcFiles.entrySet()) {
				Files.write(
						Path.of("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Thumper\\cache\\" + pc.getKey()),
						pc.getValue());
			}
		}

		// Create level listing
		LevelListingFile listings = new LevelListingFile();
		listings.enteries = new ArrayList<>();

		for (int i = 0; i < assets.size(); ++i) {
			var asset = assets.get(i);
			var entry = new LevelListingFile.Entry();
			entry.key = asset.localizationKey;
			entry.unknown0 = 0;
			entry.path = String.format("levels/custom/%s.objlib", asset.levelName);
			entry.unlocks = "";
			entry.defaultLocked = false;
			entry.unknown1 = false;
			entry.triggersCredits = false;
			entry.colorIndex0 = i;
			entry.colorIndex1 = i + (assets.size() + 1);
			listings.enteries.add(entry);
		}

		{
			var entry = new LevelListingFile.Entry();
			entry.key = "level3";
			entry.unknown0 = 0;
			entry.path = "levels/level3/level_3a.objlib";
			entry.unlocks = "";
			entry.defaultLocked = false;
			entry.unknown1 = false;
			entry.triggersCredits = false;
			entry.colorIndex0 = assets.size();
			entry.colorIndex1 = assets.size() + (assets.size() + 1);
			listings.enteries.add(entry);
		}

		{
			AuroraWriter out = new AuroraWriter();
			out.obj(listings);
			Files.write(
					Path.of(String.format("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Thumper\\cache\\%s.pc",
							Integer.toHexString(Hash.fnv1a("Aui/thumper.levels")))),
					out.getBytes());
		}

		{
			String path = String.format("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Thumper\\cache\\%s.pc",
					Integer.toHexString(Hash.fnv1a("Aui/strings.en.loc")));

			AuroraReader in = new AuroraReader(Files.readAllBytes(Path.of(path)));

			LocalizationFile locs = new LocalizationFile();
			locs.read(in);

			for (var asset : assets) {
				int idx = locs.indexOfKey(Hash.fnv1a(asset.localizationKey));
				var entry = new LocalizationFile.Entry(asset.localizationValue, Hash.fnv1a(asset.localizationKey));
				if (idx == -1) {
					locs.enteries.add(entry);
				} else {
					locs.enteries.set(idx, entry);
				}

			}

			locs.enteries.get(locs.indexOfKey(Hash.fnv1a("level3"))).value = "FUCK YOU LEVEL 3";

			AuroraWriter out = new AuroraWriter();
			locs.write(out);

			Files.write(Path.of(path), out.getBytes());
		}
	}
}
