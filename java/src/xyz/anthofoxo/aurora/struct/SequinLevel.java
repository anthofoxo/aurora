package xyz.anthofoxo.aurora.struct;

import java.util.ArrayList;
import java.util.List;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.ApproachAnimComp;
import xyz.anthofoxo.aurora.struct.comp.Comp;
import xyz.anthofoxo.aurora.struct.sequin.ParamPath;
import xyz.anthofoxo.aurora.struct.sequin.Trait;
import xyz.anthofoxo.aurora.target.Tcle3;

public class SequinLevel implements ThumperStruct {
	public static int[] header() {
		return new int[] { 51, 33, 4 };
	}

	public static class SubPath implements ThumperStruct {
		public String path;
		public int unknown;
	}

	public static class Entry implements ThumperStruct {
		public int unknown0;
		public int beatCount;
		public boolean unknown1;
		public String leafName;
		public String mainPath;
		public List<SubPath> subpaths;
		public String stepGameplay;
		public int totalBeatToThisPoint;
		public Transform transform;
		public byte unknown2;
		public byte unknown3;
	}

	public static class Loop implements ThumperStruct {
		public String sampName;
		public int beatsPerLoop;
		public int unknown;
	}

	@FixedSize(count = 3)
	public int[] header;
	public List<Comp> comps;
	public List<Trait> traits;
	public int unknown0;
	public String phase;
	public int unknown1;
	public List<Entry> enteries;
	public List<Loop> loops;
	public boolean unknown4;
	public float volume;

	/**
	 * seen used in title_screen, value was "title_init_cut.flow"
	 */
	public String startFlow;

	/**
	 * this is usually an empty list, in title_screen this is seen as a param list
	 */
	public List<ParamPath> unknown3;

	public String traitType;
	public boolean inputAllowed;
	public String tutorialType;
	public Vec3f startAngleFracs;

	public static SequinLevel in(AuroraReader in) {
		SequinLevel instance = new SequinLevel();
		instance.header = in.i32arr(3);
		instance.comps = in.objlist(Comp.class);
		instance.traits = in.objlist(Trait.class);
		instance.unknown0 = in.i32();
		instance.phase = in.str();
		instance.unknown1 = in.i32();

		instance.enteries = new ArrayList<>();

		while (in.bool()) {
			instance.enteries.add(in.obj(Entry.class));
		}

		instance.loops = in.objlist(Loop.class);
		instance.unknown4 = in.bool();
		instance.volume = in.f32();
		instance.startFlow = in.str();
		instance.unknown3 = in.objlist(ParamPath.class);
		instance.traitType = in.str();
		instance.inputAllowed = in.bool();
		instance.tutorialType = in.str();
		instance.startAngleFracs = in.obj(Vec3f.class);

		return instance;

	}

	public JsonNode toTcle3(String declarationName) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.createObjectNode();

		node.put("obj_type", "SequinLevel");
		node.put("obj_name", declarationName);

		node.set("seq_objs", mapper.createArrayNode());

		int approachBeats = 0;

		for (var comp : comps) {
			if (comp instanceof ApproachAnimComp c) {
				approachBeats = c.approachBeats;
			}
		}

		node.put("approach_beats", approachBeats);
		node.put("volume", volume);
		node.put("input_allowed", inputAllowed);
		node.put("tutorial_type", tutorialType);
		node.set("start_angle_fracs", Tcle3.fromVec3f(startAngleFracs));

		ArrayNode nLoops = mapper.createArrayNode();

		for (var loop : loops) {
			var nLoop = mapper.createObjectNode();
			nLoop.put("samp_name", loop.sampName);
			nLoop.put("beats_per_loop", loop.beatsPerLoop);
			nLoops.add(nLoop);
		}

		node.set("loops", nLoops);

		ArrayNode nLeafs = mapper.createArrayNode();

		for (var leaf : enteries) {
			var nLeaf = mapper.createObjectNode();

			nLeaf.put("beat_cnt", leaf.beatCount);
			nLeaf.put("leaf_name", leaf.leafName);
			nLeaf.put("main_path", leaf.mainPath);

			nLeaf.set("pos", Tcle3.fromVec3f(leaf.transform.pos));
			nLeaf.set("rot_x", Tcle3.fromVec3f(leaf.transform.rotx));
			nLeaf.set("rot_y", Tcle3.fromVec3f(leaf.transform.roty));
			nLeaf.set("rot_z", Tcle3.fromVec3f(leaf.transform.rotz));
			nLeaf.set("scale", Tcle3.fromVec3f(leaf.transform.scale));

			var nSubpaths = mapper.createArrayNode();

			for (var path : leaf.subpaths) {
				nSubpaths.add(path.path);
			}

			nLeaf.set("sub_paths", nSubpaths);

			nLeafs.add(nLeaf);
		}

		node.set("leaf_seq", nLeafs);

		return node;
	}
}
