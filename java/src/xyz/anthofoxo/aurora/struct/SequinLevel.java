package xyz.anthofoxo.aurora.struct;

import java.util.ArrayList;
import java.util.List;

import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.yaml.YAMLMapper;
import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.AnimComp;
import xyz.anthofoxo.aurora.struct.comp.Comp;
import xyz.anthofoxo.aurora.struct.comp.EditStateComp;
import xyz.anthofoxo.aurora.struct.sequin.ParamPath;
import xyz.anthofoxo.aurora.struct.sequin.Trait;

public class SequinLevel implements ThumperStruct {
	public static int[] header() {
		return new int[] { 51, 33, 4 };
	}

	public static class SubPath implements ThumperStruct {
		public String path;
		public int unknown;

		public ObjectNode toAur() {
			YAMLMapper mapper = new YAMLMapper();
			var node = mapper.createObjectNode();

			node.put("path", path);
			node.put("unknown", unknown);

			return node;
		}
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

		public ObjectNode toAur() {
			YAMLMapper mapper = new YAMLMapper();
			var node = mapper.createObjectNode();

			node.put("unknown0", unknown0);
			node.put("beatCount", beatCount);
			node.put("unknown1", unknown1);
			node.put("leafName", leafName);
			node.put("mainPath", mainPath);
			var subpathsNode = mapper.createArrayNode();
			for (var subpath : subpaths) subpathsNode.add(subpath.toAur());
			node.set("subpaths", subpathsNode);
			node.put("stepGameplay", stepGameplay);
			node.put("totalBeatToThisPoint", totalBeatToThisPoint);
			node.set("transform", transform.toAur());
			node.put("unknown2", unknown2);
			node.put("unknown3", unknown3);

			return node;
		}
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

		// public List<Trait> traits;

		root.put("unknown0", unknown0);
		root.put("phase", phase);
		root.put("unknown1", unknown1);

		{
			var node = mapper.createArrayNode();
			for (var value : enteries) {
				node.add(value.toAur());
			}
			root.set("entries", node);
		}

		{
			var node = mapper.createArrayNode();
			for (var value : loops) {
				var n = mapper.createObjectNode();
				n.put("sampName", value.sampName);
				n.put("beatsPerLoop", value.beatsPerLoop);
				n.put("unknown", value.unknown);
				node.add(n);
			}
			root.set("loops", node);
		}

		root.put("unknown4", unknown4);
		root.put("volume", volume);

		root.put("startFlow", startFlow);

		{
			var node = mapper.createArrayNode();
			for (var value : unknown3) {
				node.add(value.toAur());
			}
			root.set("unknown3", node);
		}

		root.put("traitType", traitType);
		root.put("inputAllowed", inputAllowed);
		root.put("tutorialType", tutorialType);

		root.set("startAngleFracs", startAngleFracs.toAur());

		return root;
	}

}
