package xyz.anthofoxo.aurora.struct;

import java.util.ArrayList;
import java.util.List;

import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.struct.SequinGate.ParamPath;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class SequinLeaf implements ThumperStruct {
	public static class Loop implements ThumperStruct {
		public String sampleName;
		public int beatsPerLoop;
		public int unknown;
	}

	public static class DataPointList implements ThumperStruct {
		public int traitType;
		public List<DataPoint> datapoints;
		public List<DataPoint> editorpoints;

		public static DataPointList in(AuroraReader in) {
			var instance = new DataPointList();
			instance.traitType = in.i32();

			int numDatapoints = in.i32();
			instance.datapoints = new ArrayList<>(numDatapoints);

			for (int i = 0; i < numDatapoints; ++i) {
				instance.datapoints.add(DataPoint.read(in, instance.traitType));
			}

			int numDatapointsEditor = in.i32();
			instance.editorpoints = new ArrayList<>(numDatapointsEditor);

			for (int i = 0; i < numDatapointsEditor; ++i) {
				instance.editorpoints.add(DataPoint.read(in, instance.traitType));
			}

			return instance;
		}
	}

	public static class DataPoint {
		public float beat;
		public Object data;
		public String interp;
		public String ease;

		public static DataPoint read(AuroraReader in, int type) {
			DataPoint d = new DataPoint();
			d.beat = in.f32();

			switch (type) {
			case 0:
				d.data = in.i32();
				break;
			case 1:
				d.data = in.bool();
				break;
			case 2:
				d.data = in.f32();
				break;
			case 3:
				d.data = in.obj(Vec4f.class);
				break;
			case 4:
				d.data = in.str();
				break;
			case 5:
				d.data = in.obj(Vec3f.class);
				break;
			case 8:
				d.data = in.bool();
				break;
			default:
				throw new IllegalStateException("Unsupported type: " + type);
			}

			d.interp = in.str();
			d.ease = in.str();

			return d;
		}

		@Override
		public String toString() {
			return String.format("%.1f : %s", beat, data.toString());
		}
	}

	public static class Trait implements ThumperStruct {
		public String objName;
		public List<ParamPath> params;
		public DataPointList datapoints;

		@FixedSize(count = 5)
		public int[] footer0;
		public String footer1;
		public String footer2;
		public boolean footer3;
		public boolean footer4;
		public int footer5;
		@FixedSize(count = 5)
		public float[] footer6;
		public boolean footer7;
		public boolean footer8;
		public boolean footer9;
	}

	public static int[] header() {
		return new int[] { 34, 33, 4 };
	}

	@FixedSize(count = 3)
	public int[] header;
	public List<Comp> comps;
	public List<Trait> objects;
	public int unknown0;
	public List<Vec3f> unknownBeatFooter;
	public Vec3f finalFooter;
}
