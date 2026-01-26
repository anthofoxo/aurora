package xyz.anthofoxo.aurora.struct;

import imgui.ImVec4;
import tools.jackson.databind.JsonNode;

public class Vec4f implements ThumperStruct {
	public float x;
	public float y;
	public float z;
	public float w;

	public Vec4f() {
	}

	public Vec4f(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public ImVec4 toImVec4wxyz(ImVec4 dest) {
		if (dest == null) dest = new ImVec4();
		dest.set(w, x, y, z);
		return dest;
	}

	public static Vec4f parse(JsonNode node) {
		return new Vec4f(node.get(0).asFloat(), node.get(1).asFloat(), node.get(2).asFloat(), node.get(3).asFloat());
	}
}
