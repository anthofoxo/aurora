package xyz.anthofoxo.aurora.struct.objlib;

import xyz.anthofoxo.aurora.struct.ThumperStruct;

public class LibraryImport implements ThumperStruct {
	public int unknown;
	public String path;

	public LibraryImport() {
	}

	public LibraryImport(int unknown, String path) {
		this.unknown = unknown;
		this.path = path;
	}
}