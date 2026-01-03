package xyz.anthofoxo.aurora.target;

import java.awt.Color;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.system.MemoryUtil;

import tools.jackson.databind.JsonNode;
import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.Util;
import xyz.anthofoxo.aurora.gfx.Texture;
import xyz.anthofoxo.aurora.parse.AuroraWriter;
import xyz.anthofoxo.aurora.struct.EntitySpawner;
import xyz.anthofoxo.aurora.struct.PrecompiledBin;
import xyz.anthofoxo.aurora.struct.Sample;
import xyz.anthofoxo.aurora.struct.SectionFile;
import xyz.anthofoxo.aurora.struct.SequinGate;
import xyz.anthofoxo.aurora.struct.SequinGate.ParamPath;
import xyz.anthofoxo.aurora.struct.SequinLeaf;
import xyz.anthofoxo.aurora.struct.SequinLeaf.DataPoint;
import xyz.anthofoxo.aurora.struct.SequinLeaf.DataPointList;
import xyz.anthofoxo.aurora.struct.SequinLeaf.Trait;
import xyz.anthofoxo.aurora.struct.SequinLevel;
import xyz.anthofoxo.aurora.struct.SequinLevel.Loop;
import xyz.anthofoxo.aurora.struct.SequinLevel.SubPath;
import xyz.anthofoxo.aurora.struct.SequinMaster;
import xyz.anthofoxo.aurora.struct.Transform;
import xyz.anthofoxo.aurora.struct.Vec3f;
import xyz.anthofoxo.aurora.struct.Vec4f;
import xyz.anthofoxo.aurora.struct.Xfmer;
import xyz.anthofoxo.aurora.struct.comp.AnimComp;
import xyz.anthofoxo.aurora.struct.comp.ApproachAnimComp;
import xyz.anthofoxo.aurora.struct.comp.EditStateComp;
import xyz.anthofoxo.aurora.struct.comp.XfmComp;
import xyz.anthofoxo.aurora.tml.TCLFile;

public class Tcle3 extends Target {
	/**
	 * These are a list of file types that contain json objects and are to be parsed
	 * as it into the object tree
	 */
	private static final List<String> JSON_TYPES = List.of(".gate", ".leaf", ".lvl", ".master", ".xfm", ".config");

	/**
	 * These are a list of file types that contain json arrays, each element of the
	 * json node should be added independently
	 */
	private static final List<String> JSON_MULTI_TYPES = List.of(".spn", ".samp");

	/**
	 * Contains a list of object types
	 */
	private static final List<String> OBJ_TYPES = List.of("SequinLeaf", "SequinLevel", "SequinGate", "SequinMaster",
			"EntitySpawner", "Sample", "Xfmer");

	private List<Path> paths = new ArrayList<>();

	public Tcle3(Path path) throws IOException {
		try (var stream = Files.walk(path)) {
			for (var entry : stream.collect(Collectors.toList())) {
				if (Files.isDirectory(entry)) continue;

				if (".png".equals(getExtension(entry.toString()))) {

					var bytes = Files.readAllBytes(entry);
					ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);

					try {
						buffer.put(0, bytes);
						texture = Texture.makeFromPNG(buffer);
					} finally {
						MemoryUtil.memFree(buffer);
					}
				} else if (".tcl".equals(getExtension(entry.toString()))) {
					tcl = TCLFile.parse(JSON_MAPPER.readTree(Files.readAllBytes(entry)));
				}

				paths.add(entry);
			}
		}

		if (tcl == null) throw new IllegalStateException("No TCL file found");
	}

	public static ParamPath parseParamPath(String param_path, String param_path_hash) {

		String param;
		String param_name;
		String param_idx;

		if (!isNullOrEmpty(param_path)) param = param_path;
		else param = param_path_hash;
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

		int hashedName;

		// depending if the string is plain text or hex-hash, write it to .pc file
		// differently
		if (!isNullOrEmpty(param_path)) hashedName = Hash.fnv1a(param_name);
		else {
			byte[] str = stringToByteArray(param_name);
			assert (str.length == 4);

			hashedName = 0;
			hashedName |= (str[0] & 0xFF) << 24;
			hashedName |= (str[1] & 0xFF) << 16;
			hashedName |= (str[2] & 0xFF) << 8;
			hashedName |= (str[3] & 0xFF);
		}

		return new ParamPath(hashedName, Integer.parseInt(param_idx));
	}

	private static byte[] stringToByteArray(String hex) {
		int len = hex.length();
		byte[] bytes = new byte[len / 2];

		for (int i = 0; i < len; i += 2) {
			bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
		}

		return bytes;
	}

	public static SequinGate toSequinGate(JsonNode obj) {
		SequinGate gate = new SequinGate().withTMLDefaults();
		gate.entitySpawnerName = obj.get("spn_name").asString();
		gate.params = List.of(parseParamPath(obj.get("param_path"), obj.get("param_path_hash")));

		for (var boss_pattern : obj.get("boss_patterns")) {
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

		gate.preLevelName = obj.get("pre_lvl_name").asString();
		gate.postLevelName = obj.get("post_lvl_name").asString();
		gate.restartLevelName = obj.get("restart_lvl_name").asString();
		gate.sectionType = obj.get("section_type").asString();
		gate.randomType = obj.get("random_type").asString();

		return gate;
	}

	@Override
	public CompiledTarget build(float speedModifier) throws IOException {
		CompiledTarget compiled = new CompiledTarget();
		compiled.levelName = tcl.levelName;

		List<JsonNode> objs = new ArrayList<>();

		// The precompiled bins are built using level 9 as a base
		// One of the included assets expect this leaf to exist
		objs.add(JSON_MAPPER.readTree(Util.getResourceBytes("leaf_pyramid_outro.txt")));

		// Iterate over all files and put them into the obj list
		// If they are .pc files, then directly load them into the compilation target

		for (var entry : paths) {
			final String ext = getExtension(entry.toString());

			// we can ignore .tcl here as it gets loaded in the constructor

			if (JSON_TYPES.contains(ext)) {
				objs.add(JSON_MAPPER.readTree(entry));
			} else if (JSON_MULTI_TYPES.contains(ext)) {
				var obj = JSON_MAPPER.readTree(entry);
				for (var _v : obj.get("items")) objs.add(_v);
			} else if (".pc".equals(ext)) {
				int idx = entry.toString().replace("\\", "/").lastIndexOf('/');
				String filename = entry.toString();
				if (idx != -1) filename = filename.substring(idx + 1);
				compiled.pcFiles.put(filename, Files.readAllBytes(entry));
			}
		}

		AuroraWriter writer = new AuroraWriter();
		writer.i8arr(PrecompiledBin.getHeaderBin());
		writer.str(String.format("levels/custom/%s.objlib", tcl.levelName));
		writer.i8arr(PrecompiledBin.getObjList1Bin());

		writer.i32(PrecompiledBin.getObjListCount() + objs.size());
		writer.i8arr(PrecompiledBin.getObjList2Bin());

		for (var obj : objs) {
			if (OBJ_TYPES.contains(obj.get("obj_type").asString())) {
				if (!obj.get("obj_type").asString().equals("Xfmer")) {
					writer.hash(obj.get("obj_type").asString());
					writer.str(obj.get("obj_name").asString());
				} else {
					writer.hash(obj.get("obj_type").asString());
					writer.str(String.format("levels/custom/%s.xfm", tcl.levelName));
				}
			}
		}

		writer.i8arr(PrecompiledBin.getObjDef0());
		writer.i8arr(Util.getResourceBytes("obj_def_customlevel.objlib"));

		for (var obj : objs) {
			final String objType = obj.get("obj_type").asString();

			if (objType.equals("SequinLeaf")) writer.obj(toSequinLeaf(obj));
			else if (objType.equals("SequinLevel")) writer.obj(toSequinLevel(obj));
			else if (objType.equals("SequinGate")) writer.obj(toSequinGate(obj));
			else if (objType.equals("SequinMaster")) writer.obj(toSequinMaster(obj));
			else if (objType.equals("EntitySpawner")) writer.obj(toEntiySpawner(obj));
			else if (objType.equals("Sample")) {
				var sample = toSample(obj);
				sample.pitch *= speedModifier;
				if (speedModifier != 1.0f) {
					sample.channelGroup = "sequin.ch";
				}
				writer.obj(sample);
			} else if (objType.equals("Xfmer")) {
				Xfmer xfm = new Xfmer();
				xfm.header = Xfmer.header();
				xfm.comps = List.of(toXfmComp(obj));
				writer.obj(xfm);
			}
		}

		writer.i8arr(Util.getResourceBytes("footer_1.objlib"));
		writer.f32(tcl.bpm * speedModifier);
		writer.i8arr(Util.getResourceBytes("footer_2.objlib"));

		AuroraWriter sec = new AuroraWriter();

		sec.obj(SectionFile.fromTML(tcl));

		compiled.objlib = writer.getBytes();
		compiled.sec = sec.getBytes();
		compiled.localizationKey = String.format("custom.%s", tcl.levelName);
		compiled.localizationValue = tcl.levelName;

		return compiled;
	}

	public static SequinMaster toSequinMaster(JsonNode obj) {
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

	public static ParamPath parseParamPath(JsonNode paramPathNode, JsonNode paramPathHashNode) {
		String pp = null;
		String pph = null;

		if (paramPathNode != null) pp = paramPathNode.asString();
		if (paramPathHashNode != null) pph = paramPathHashNode.asString();

		return parseParamPath(pp, pph);
	}

	private static List<String> trait_types = Arrays.asList("kTraitInt", "kTraitBool", "kTraitFloat", "kTraitColor",
			"kTraitObj", "kTraitVec3", "kTraitPath", "kTraitEnum", "kTraitAction", "kTraitObjVec", "kTraitString",
			"kTraitCue", "kTraitEvent", "kTraitSym", "kTraitList", "kTraitTraitPath", "kTraitQuat", "kTraitChildLib",
			"kTraitComponent", "kNumTraitTypes");

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

	private static void setDataPoint(DataPoint p, String val, String trait_type) {

		if (trait_type.equals("kTraitInt")) p.data = Integer.parseInt(val);
		else if (trait_type.equals("kTraitBool") || trait_type.equals("kTraitAction")) {
			p.data = toBoolean(val);
		} else if (trait_type.equals("kTraitFloat")) p.data = Float.parseFloat(val);
		else if (trait_type.equals("kTraitColor")) {
			try {
				Color c = new Color((int) Double.parseDouble(val), true);
				p.data = new Vec4f(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
			} catch (NumberFormatException e) {
				p.data = new Vec4f(1, 1, 1, 1);
			}
		}
	}

	private static List<Trait> toTraitList(JsonNode obj) {

		int beat_cnt = 0;
		if (obj.has("beat_cnt")) beat_cnt = obj.get("beat_cnt").asInt();

		var seq_objs = obj.get("seq_objs");
		// write amount of seq_objs (different tracks) to .pc file
		// int size = seq_objs.size();

		List<Trait> traits = new ArrayList<>();

		for (var _obj : seq_objs) {
			String obj_name = _obj.get("obj_name").asString();

			if (obj_name.startsWith("_")) continue;

			Trait trait = new Trait();
			trait.objName = obj_name;
			trait.params = List.of(parseParamPath(_obj.get("param_path"), _obj.get("param_path_hash")));

			{
				trait.datapoints = new DataPointList();
				trait.datapoints.traitType = trait_types.indexOf(_obj.get("trait_type").asString());
				trait.datapoints.editorpoints = List.of();
				trait.datapoints.datapoints = new ArrayList<>();

				String traittype = _obj.get("trait_type").asString();
				String default_value = _obj.get("default").asString();

				// Data points written different depending on STEP
				if (asBool(_obj.get("step")) == true) {
					/// STEP true = value updates every beat, and if no value is set for a beat,
					/// it'll use _obj.default
					int indexofwrittenbeat = 0;
					for (int i = 0; i < beat_cnt; i++) {
						DataPoint datapoint = new DataPoint();
						datapoint.beat = i;

						var node = _obj.get("data_points").get(indexofwrittenbeat);

						if (_obj.get("data_points").size() > indexofwrittenbeat && node.get("beat").asInt() == i) {
							setDataPoint(datapoint, node.get("value").asString(), traittype);
							datapoint.interp = node.get("interp").asString();
							datapoint.ease = node.get("ease").asString();
							indexofwrittenbeat++;
						} else {
							setDataPoint(datapoint, default_value, traittype);
							datapoint.interp = "kTraitInterpLinear";
							datapoint.ease = "kEaseInOut";
						}

						trait.datapoints.datapoints.add(datapoint);
					}
				} else {
					/// STEP false = value interpolates between values set on beats. Default
					/// is ignored.

					for (var dp : _obj.get("data_points")) {
						DataPoint datapoint = new DataPoint();

						datapoint.beat = dp.get("beat").asFloat();
						setDataPoint(datapoint, dp.get("value").asString(), traittype);
						datapoint.interp = dp.get("interp").asString();
						datapoint.ease = dp.get("ease").asString();

						trait.datapoints.datapoints.add(datapoint);
					}
				}
			}

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

			// @formatter:off
			trait.footer0 = new int[] {
					Integer.parseInt(_footer[0]),
					Integer.parseInt(_footer[1]),
					Integer.parseInt(_footer[2]),
					Integer.parseInt(_footer[3]),
					Integer.parseInt(_footer[4]),
			};
			// @formatter:on

			trait.footer1 = _footer[5];
			trait.footer2 = _footer[6];
			trait.footer3 = toBoolean(_footer[7]);
			trait.footer4 = toBoolean(_footer[8]);
			trait.footer5 = Integer.parseInt(_footer[9]);

			// @formatter:off
			trait.footer6 = new float[] {
					Float.parseFloat(_footer[10]),
					Float.parseFloat(_footer[11]),
					Float.parseFloat(_footer[12]),
					Float.parseFloat(_footer[13]),
					Float.parseFloat(_footer[14]),
			};
			// @formatter:on

			trait.footer7 = toBoolean(_footer[15]);
			trait.footer8 = toBoolean(_footer[16]);
			trait.footer9 = toBoolean(_footer[17]);

			traits.add(trait);
		}

		return traits;

	}

	private static SequinLeaf toSequinLeaf(JsonNode obj) {
		SequinLeaf instance = new SequinLeaf();
		instance.header = SequinLeaf.header();
		instance.comps = List.of(new AnimComp(), new EditStateComp());
		instance.objects = toTraitList(obj);
		instance.unknown0 = 0;
		int beat_cnt = obj.get("beat_cnt").asInt();
		instance.unknownBeatFooter = new ArrayList<>(beat_cnt);
		for (int i = 0; i < beat_cnt; i++) instance.unknownBeatFooter.add(new Vec3f());
		instance.finalFooter = new Vec3f();
		return instance;
	}

	public static SequinLevel toSequinLevel(JsonNode obj) {
		SequinLevel instance = new SequinLevel();
		instance.header = SequinLevel.header();
		instance.comps = List.of(new ApproachAnimComp().withApproachBeats(obj.get("approach_beats").asInt()),
				new EditStateComp());

		instance.traits = toTraitList(obj);
		instance.unknown0 = 0;
		instance.phase = "kMovePhaseRepeatChild";
		instance.unknown1 = 0;
		instance.enteries = new ArrayList<SequinLevel.Entry>();

		int last_beat_cnt = 0;

		for (var leaf : obj.get("leaf_seq")) {
			SequinLevel.Entry entry = new SequinLevel.Entry();

			entry.unknown0 = 0;
			entry.beatCount = leaf.get("beat_cnt").asInt();
			entry.unknown1 = true;
			entry.leafName = leaf.get("leaf_name").asString();
			entry.mainPath = leaf.get("main_path").asString();

			entry.subpaths = new ArrayList<>();
			for (var sub_path : leaf.get("sub_paths")) {
				SubPath path = new SubPath();
				path.path = sub_path.asString();
				path.unknown = 0;
				entry.subpaths.add(path);
			}

			entry.stepGameplay = "kStepGameplay";
			entry.totalBeatToThisPoint = last_beat_cnt;
			entry.transform = toTransform(leaf);
			entry.unknown2 = 0;
			entry.unknown3 = 0;

			instance.enteries.add(entry);

			last_beat_cnt = leaf.get("beat_cnt").asInt();
		}

		instance.loops = new ArrayList<>();

		for (var loop : obj.get("loops")) {
			var obje = new Loop();
			obje.sampName = loop.get("samp_name").asString();
			obje.beatsPerLoop = loop.get("beats_per_loop").asInt();
			obje.unknown = 0;
			instance.loops.add(obje);
		}

		instance.unknown4 = false;
		instance.volume = obj.get("volume").asFloat();
		instance.startFlow = "";
		instance.unknown3 = List.of();
		instance.traitType = "kNumTraitTypes";
		instance.inputAllowed = asBool(obj.get("input_allowed"));
		instance.tutorialType = obj.get("tutorial_type").asString();
		instance.startAngleFracs = toVec3f(obj.get("start_angle_fracs"));

		return instance;
	}

	private static boolean asBool(JsonNode node) {
		if (node == null || node.isNull()) return false;
		if (node.isBoolean()) return node.asBoolean();
		return Boolean.parseBoolean(node.asString());
	}

	private static Vec3f toVec3f(JsonNode obj) {
		return new Vec3f(obj.get(0).asFloat(), obj.get(1).asFloat(), obj.get(2).asFloat());
	}

	private static Transform toTransform(JsonNode obj) {
		Transform t = new Transform();
		t.pos = toVec3f(obj.get("pos"));
		t.rotx = toVec3f(obj.get("rot_x"));
		t.roty = toVec3f(obj.get("rot_y"));
		t.rotz = toVec3f(obj.get("rot_z"));
		t.scale = toVec3f(obj.get("scale"));
		return t;
	}

	private static XfmComp toXfmComp(JsonNode obj) {
		return new XfmComp(obj.get("xfm_name").asString(), obj.get("constraint").asString(), toTransform(obj));
	}

	private static EntitySpawner toEntiySpawner(JsonNode obj) {
		EntitySpawner spn = new EntitySpawner();
		spn.header = EntitySpawner.header();
		spn.comps = List.of(new EditStateComp(), toXfmComp(obj));
		spn.unknown = 0;
		spn.objlibPath = obj.get("objlib_path").asString();
		spn.bucket = obj.get("bucket").asString();
		return spn;
	}

	private static Sample toSample(JsonNode obj) {
		Sample sample = new Sample();
		sample.header = Sample.header();
		sample.comps = List.of(new EditStateComp());
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

}
