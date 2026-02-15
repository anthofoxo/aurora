package xyz.anthofoxo.aurora.struct;

import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.yaml.YAMLMapper;

public class Transform implements ThumperStruct {
	public Vec3f pos;
	public Vec3f rotx;
	public Vec3f roty;
	public Vec3f rotz;
	public Vec3f scale;

	public static Transform identity() {
		Transform t = new Transform();
		t.pos = new Vec3f(0, 0, 0);
		t.rotx = new Vec3f(1, 0, 0);
		t.roty = new Vec3f(0, 1, 0);
		t.rotz = new Vec3f(0, 0, 1);
		t.scale = new Vec3f(1, 1, 1);
		return t;
	}

	public static Transform identityScaled(float scaleX, float scaleY, float scaleZ) {
		Transform t = identity();
		t.scale.x = scaleX;
		t.scale.y = scaleY;
		t.scale.z = scaleZ;
		return t;
	}

	public ObjectNode toAur() {
		YAMLMapper mapper = new YAMLMapper();
		var node = mapper.createObjectNode();
		node.set("pos", pos.toAur());
		node.set("rotx", rotx.toAur());
		node.set("roty", roty.toAur());
		node.set("rotz", rotz.toAur());
		node.set("scale", scale.toAur());
		return node;
	}
}
