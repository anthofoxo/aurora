package xyz.anthofoxo.aurora.struct;

import java.util.List;

public class ObjlibLevel implements ThumperStruct {
	public int fileType = 8;
	public int objlibType = 0x0B374D9E;
	@FixedSize(count = 4)
	public int[] unknownHeader = { 33, 19, 21, 4 };
	public List<LibraryImport> libraryImports;
	public String levelPath;
	public List<LibraryObject> libraryObjects;
	public List<ObjectDeclaration> objectDeclarations;
	
	// TODO: ObjlibLevel is not complete, work on expanding
}
