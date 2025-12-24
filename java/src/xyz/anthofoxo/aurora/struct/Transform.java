package xyz.anthofoxo.aurora.struct;

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
}
