package xyz.anthofoxo.aurora.struct;

public class LibraryObject implements ThumperStruct {
	public int type;
	public String name;
	public int objlibType;
	public String path;

	public LibraryObject() {
	}

	public LibraryObject(int type, String name, int objlibType, String path) {
		this.type = type;
		this.name = name;
		this.objlibType = objlibType;
		this.path = path;
	}
}