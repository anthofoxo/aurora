package xyz.anthofoxo.aurora.struct.sequin;

import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.struct.ThumperStruct;
import xyz.anthofoxo.aurora.struct.Vec3f;
import xyz.anthofoxo.aurora.struct.Vec4f;

public class DataPoint implements ThumperStruct {
	public float beat;
	public Object data;
	public String interp;
	public String ease;

	public static DataPoint read(AuroraReader in, int type) {
		DataPoint d = new DataPoint();
		d.beat = in.f32();

		switch (type) {
		case 0:
			d.data = in.i32();
			break;
		case 1:
			d.data = in.bool();
			break;
		case 2:
			d.data = in.f32();
			break;
		case 3:
			d.data = in.obj(Vec4f.class);
			break;
		case 4:
			d.data = in.str();
			break;
		case 5:
			d.data = in.obj(Vec3f.class);
			break;
		case 8:
			d.data = in.bool();
			break;
		default:
			throw new IllegalStateException("Unsupported type: " + type);
		}

		d.interp = in.str();
		d.ease = in.str();

		return d;
	}

	@Override
	public String toString() {
		return String.format("%.1f : %s", beat, data.toString());
	}
}