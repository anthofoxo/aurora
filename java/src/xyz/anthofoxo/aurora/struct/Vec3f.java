package xyz.anthofoxo.aurora.struct;

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

}
