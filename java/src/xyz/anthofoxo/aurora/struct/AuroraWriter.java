package xyz.anthofoxo.aurora.struct;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class AuroraWriter {
	private ArrayList<Byte> bytes = new ArrayList<>();

	public void bool(boolean v) {
		i8(v ? (byte) 1 : (byte) 0);
	}

	public void i8(byte value) {
		bytes.add(value);
	}

	public void i32(int v) {
		i8((byte) (v & 0xFF));
		i8((byte) ((v >>> 8) & 0xFF));
		i8((byte) ((v >>> 16) & 0xFF));
		i8((byte) ((v >>> 24) & 0xFF));
	}

	public void f32(float v) {
		i32(Float.floatToRawIntBits(v));
	}

	public void str(String v) {
		i32(v.length());
		i8arr(v.getBytes());
	}

	public void i8arr(byte... values) {
		for (var value : values) i8(value);
	}

	public void i32arr(int... values) {
		for (var value : values) i32(value);
	}

	public <T extends ThumperStruct> void objlist(List<T> values) {
		i32(values.size());
		for (var element : values) obj(element);
	}

	@SuppressWarnings("unchecked")
	public <T extends ThumperStruct> void obj(T value) {
		for (var field : value.getClass().getFields()) {
			if (Modifier.isStatic(field.getModifiers())) continue;
			var type = field.getType();

			try {
				if (boolean.class.equals(type)) bool(field.getBoolean(value));
				else if (byte.class.equals(type)) i8(field.getByte(value));
				else if (int.class.equals(type)) i32(field.getInt(value));
				else if (float.class.equals(type)) f32(field.getFloat(value));
				else if (String.class.equals(type)) str(String.class.cast(field.get(value)));
				else if (byte[].class.equals(type)) i8arr(byte[].class.cast(field.get(value)));
				else if (int[].class.equals(type)) i32arr(int[].class.cast(field.get(value)));
				else if (ThumperStruct.class.isAssignableFrom(type)) obj(ThumperStruct.class.cast(field.get(value)));
				else if (List.class.isAssignableFrom(type))
					objlist((List<? extends ThumperStruct>) List.class.cast(field.get(value)));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public byte[] getBytes() {
		byte[] array = new byte[bytes.size()];
		int index = 0;
		for (var b : bytes) array[index++] = b;
		return array;
	}

	public void removei8(int index) {
		bytes.remove(index);
	}
}
