package xyz.anthofoxo.aurora.struct;

import java.util.ArrayList;
import java.util.List;

import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.struct.SequinLeaf.Trait;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

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
	public int unknown2;
	public int unknown3;
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
		instance.unknown2 = in.i32();
		instance.unknown3 = in.i32();
		instance.traitType = in.str();
		instance.inputAllowed = in.bool();
		instance.tutorialType = in.str();
		instance.startAngleFracs = in.obj(Vec3f.class);

		return instance;

	}
}
