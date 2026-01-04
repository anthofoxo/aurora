package xyz.anthofoxo.aurora.struct.sequin;

import xyz.anthofoxo.aurora.struct.ThumperStruct;

public class ParamPath implements ThumperStruct {
	public int paramHash;
	public int paramIdx; // typically -1

	public ParamPath() {
	}

	public ParamPath(int paramHash, int paramIdx) {
		this.paramHash = paramHash;
		this.paramIdx = paramIdx;
	}
}