package xyz.anthofoxo.aurora.struct.sequin;

import java.util.ArrayList;
import java.util.List;

import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.struct.ThumperStruct;
import xyz.anthofoxo.aurora.struct.annotation.UnknownNativeName;

@UnknownNativeName
public class DataPointList implements ThumperStruct {
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
