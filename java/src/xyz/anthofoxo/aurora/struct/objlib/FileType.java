package xyz.anthofoxo.aurora.struct.objlib;

import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.struct.ThumperStruct;

public enum FileType implements ThumperStruct {
	ObjLib(8);

	public int value;

	private FileType(int value) {
		this.value = value;
	}

	public static FileType in(AuroraReader in) {
		int hash = in.i32();

		for (var v : values()) {
			if (hash == v.value) return v;
		}

		throw new IllegalStateException("No file type has the value: " + Integer.toHexString(hash));
	}
}
