package xyz.anthofoxo.aurora.struct.objlib;

import xyz.anthofoxo.aurora.struct.ThumperStruct;

public class ObjectDeclaration implements ThumperStruct {
	public DeclarationType type;
	public String name;

	public ObjectDeclaration() {
	}

	public ObjectDeclaration(DeclarationType type, String name) {
		this.type = type;
		this.name = name;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, type);
	}
}