package xyz.anthofoxo.aurora.struct;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import tools.jackson.databind.JsonNode;
import xyz.anthofoxo.aurora.Hash;

public class AuroraWriter {
	private ArrayList<Byte> bytes = new ArrayList<>();

	public int position() {
		return bytes.size();
	}

	public void bool(boolean v) {
		i8(v ? (byte) 1 : (byte) 0);
	}

	public void i8(byte value) {
		bytes.add(value);
	}

	@Deprecated
	private static byte[] stringToByteArray(String hex) {
		int len = hex.length();
		byte[] bytes = new byte[len / 2];

		for (int i = 0; i < len; i += 2) {
			bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
		}

		return bytes;
	}

	@Deprecated
	public void i8arrReverse(String v) {
		var bytes = stringToByteArray(v);
		for (int i = bytes.length - 1; i >= 0; --i) i8(bytes[i]);
	}

	@Deprecated
	public void vec3(JsonNode jsonNode) {
		f32(jsonNode.get(0).asFloat());
		f32(jsonNode.get(1).asFloat());
		f32(jsonNode.get(2).asFloat());
	}

	public void hash(String v) {
		i32(Hash.fnv1a(v));
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

	public void strlist(List<String> values) {
		i32(values.size());
		for (var element : values) str(element);
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
				else if (List.class.isAssignableFrom(type)) {
					var list = field.get(value);
					var genericType = field.getGenericType();

					if (genericType instanceof ParameterizedType pt) {
						var arg = pt.getActualTypeArguments()[0];

						if (String.class.equals(arg)) {
							strlist((List<String>) list);
						} else if (arg instanceof Class<?> c && ThumperStruct.class.isAssignableFrom(c)) {

							objlist((List<? extends ThumperStruct>) list);
						} else {
							throw new IllegalStateException("Failed to parse");
						}
					} else {
						throw new IllegalStateException("Failed to parse");
					}
				} else {
					throw new IllegalStateException("Failed to parse");
				}
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

	public void cstr(String value) {
		i8arr(value.getBytes());
		i8((byte) 0);
	}
}
