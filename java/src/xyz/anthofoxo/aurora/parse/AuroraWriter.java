package xyz.anthofoxo.aurora.parse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.struct.ThumperStruct;
import xyz.anthofoxo.aurora.struct.annotation.RemoveFieldIfEnclosed;

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

	public void i16(short v) {
		i8((byte) (v & 0xFF));
		i8((byte) ((v >>> 8) & 0xFF));
	}

	public void i32(int v) {
		i8((byte) (v & 0xFF));
		i8((byte) ((v >>> 8) & 0xFF));
		i8((byte) ((v >>> 16) & 0xFF));
		i8((byte) ((v >>> 24) & 0xFF));
	}

	public void i64(long v) {
		i8((byte) (v & 0xFF));
		i8((byte) ((v >>> 8) & 0xFF));
		i8((byte) ((v >>> 16) & 0xFF));
		i8((byte) ((v >>> 24) & 0xFF));
		i8((byte) ((v >>> 32) & 0xFF));
		i8((byte) ((v >>> 40) & 0xFF));
		i8((byte) ((v >>> 48) & 0xFF));
		i8((byte) ((v >>> 56) & 0xFF));
	}

	public void instant(Instant v) {
		i64(v.getEpochSecond());
	}

	public void f32(float v) {
		i32(Float.floatToRawIntBits(v));
	}

	public void f64(double v) {
		i64(Double.doubleToRawLongBits(v));
	}

	@Deprecated
	public void hash(String v) {
		i32(Hash.fnv1a(v));
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

	private Stack<Class<?>> enclosing = new Stack<>();

	@SuppressWarnings("unchecked")
	public <T extends ThumperStruct> void obj(T value) {
		// Push enclosing state
		enclosing.push(value.getClass());

		try {

			// Does class provide custom override?, if so then invoke it
			try {
				var clazz = value.getClass();

				var method = clazz.getMethod("out", AuroraWriter.class, clazz);

				try {
					method.invoke(null, this, value);
					return;
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new ParseException(e);
				}
			} catch (NoSuchMethodException e) {
				// Class doesn't have custom overload, we can safely ignore and parse as normal
			}

			// Iterate over fields
			for (var field : value.getClass().getFields()) {
				// Ignore static fields, fields with _ prefix are marked for internal use only
				if (Modifier.isStatic(field.getModifiers())) continue;
				if (field.getName().startsWith("_")) continue;

				// Check if the field removal context is valid
				var removalAnnotation = field.getAnnotation(RemoveFieldIfEnclosed.class);
				if (removalAnnotation != null) {
					if (enclosing.contains(removalAnnotation.clazz())) continue;
				}

				var type = field.getType();

				try {
					var fvalue = field.get(value);

					// Check basic types
					if (fvalue instanceof Boolean v) bool(v);
					else if (fvalue instanceof Byte v) i8(v);
					else if (fvalue instanceof Short v) i16(v);
					else if (fvalue instanceof Integer v) i32(v);
					else if (fvalue instanceof Long v) i64(v);
					else if (fvalue instanceof Float v) f32(v);
					else if (fvalue instanceof Double v) f64(v);
					else if (fvalue instanceof String v) str(v);
					else if (fvalue instanceof byte[] v) i8arr(v);
					else if (fvalue instanceof int[] v) i32arr(v);
					else if (fvalue instanceof Instant v) instant(v);
					else if (ThumperStruct.class.isAssignableFrom(type))
						obj(ThumperStruct.class.cast(field.get(value)));
					else if (List.class.isAssignableFrom(type)) {
						var list = field.get(value);

						if (field.getGenericType() instanceof ParameterizedType pt) {
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
						throw new IllegalStateException("Failed to parse " + value.getClass());
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}

		} finally {
			enclosing.pop();
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
