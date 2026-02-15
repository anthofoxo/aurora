package xyz.anthofoxo.aurora.struct;

import tools.jackson.databind.node.ArrayNode;
import tools.jackson.dataformat.yaml.YAMLMapper;

public class Vec3f implements ThumperStruct {
	public float x;
	public float y;
	public float z;

	public Vec3f() {
	}

	public Vec3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public String toString() {
		return String.format("[%.1f, %.1f, %.1f]", x, y, z);
	}

	public ArrayNode toAur() {
		YAMLMapper mapper = new YAMLMapper();
		var node = mapper.createArrayNode();
		node.add(x);
		node.add(y);
		node.add(z);
		return node;
	}

}
