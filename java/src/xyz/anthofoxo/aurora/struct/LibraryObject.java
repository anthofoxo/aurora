package xyz.anthofoxo.aurora.struct;

public class LibraryObject implements ThumperStruct {
	public int type;
	public String name;
	public int unknown;
	public String path;

	public LibraryObject() {
	}

	public LibraryObject(int type, String name, int unknown, String path) {
		this.type = type;
		this.name = name;
		this.unknown = unknown;
		this.path = path;
	}
}