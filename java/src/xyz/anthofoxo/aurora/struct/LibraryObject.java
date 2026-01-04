package xyz.anthofoxo.aurora.struct;

public class LibraryObject implements ThumperStruct {
	public LibraryType libType;
	public String name;
	public int objlibType;
	public String path;

	public LibraryObject() {
	}

	public LibraryObject(LibraryType type, String name, int objlibType, String path) {
		this.libType = type;
		this.name = name;
		this.objlibType = objlibType;
		this.path = path;
	}
}