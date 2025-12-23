package xyz.anthofoxo.aurora.struct;

public class ObjectDeclaration implements ThumperStruct {
	public DeclarationType type;
	public String name;

	public ObjectDeclaration() {
	}

	public ObjectDeclaration(DeclarationType type, String name) {
		this.type = type;
		this.name = name;
	}
}