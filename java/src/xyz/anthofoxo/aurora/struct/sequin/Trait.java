package xyz.anthofoxo.aurora.struct.sequin;

import java.util.List;

import xyz.anthofoxo.aurora.struct.ThumperStruct;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;

public class Trait implements ThumperStruct {
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
