package xyz.anthofoxo.aurora.struct;

public class Vec2f implements ThumperStruct {
	public float x;
	public float y;

	public Vec2f() {
	}

	public Vec2f(float x, float y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return String.format("[%.1f, %.1f]", x, y);
	}

}
