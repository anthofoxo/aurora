package xyz.anthofoxo.aurora.struct;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import xyz.anthofoxo.aurora.struct.comp.EditStateComp;

public class AuroraReader {
	public byte[] bytes;
	public int pos;

	public AuroraReader(byte[] bytes) {
		this.bytes = bytes;
	}

	public boolean bool() {
		return i8() == 1 ? true : false;
	}

	public byte i8() {
		return bytes[pos++];
	}

	/// bytes are in little endian
	public int i32() {
		int b0 = i8() & 0xFF;
		int b1 = i8() & 0xFF;
		int b2 = i8() & 0xFF;
		int b3 = i8() & 0xFF;
		return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);
	}

	public void seek(int newPos) {
		if (newPos < 0 || newPos > bytes.length) {
			throw new IndexOutOfBoundsException();
		}
		pos = newPos;
	}

	public int seekToi8(byte... pattern) {
		int startPos = pos;

		outer: for (int i = startPos; i <= bytes.length - pattern.length; i++) {
			for (int j = 0; j < pattern.length; j++) {
				if (bytes[i + j] != pattern[j]) {
					continue outer;
				}
			}

			// Match found: position at start of match
			pos = i;
			return i - startPos;
		}

		return -1;
	}

	public int seekToi32(int... pattern) {
		byte[] bytePattern = new byte[Integer.BYTES * pattern.length];

		int offset = 0;
		for (int i = 0; i < pattern.length; ++i) {
			int val = Integer.reverseBytes(pattern[i]);

			bytePattern[offset++] = (byte) (val);
			bytePattern[offset++] = (byte) (val >>> 8);
			bytePattern[offset++] = (byte) (val >>> 16);
			bytePattern[offset++] = (byte) (val >>> 24);
		}

		return seekToi8(bytePattern);
	}

	public float f32() {
		return Float.intBitsToFloat(i32());
	}

	public String str() {
		int length = i32();
		byte[] data = i8arr(length);
		return new String(data);
	}

	public byte[] i8arr(int count) {
		byte[] bytes = new byte[count];
		for (int i = 0; i < count; ++i) bytes[i] = i8();
		return bytes;
	}

	public int[] i32arr(int count) {
		int[] values = new int[count];
		for (int i = 0; i < count; ++i) values[i] = i32();
		return values;
	}

	public List<String> strlist() {
		int count = i32();
		List<String> values = new ArrayList<String>();
		for (int i = 0; i < count; ++i) values.add(str());
		return values;
	}

	public <T extends ThumperStruct> T comp(Class<T> clazz) {
		int hash = i32();
		if (hash == EditStateComp.HASH) return clazz.cast(new EditStateComp());
		throw new IllegalStateException();
	}

	public <T extends ThumperStruct> List<T> objlist(Class<T> clazz) {
		int count = i32();

		if (Comp.class.isAssignableFrom(clazz)) {
			List<T> values = new ArrayList<T>();
			for (int i = 0; i < count; ++i) values.add(comp(clazz));
			return values;

		} else {
			List<T> values = new ArrayList<T>();
			for (int i = 0; i < count; ++i) values.add(obj(clazz));
			return values;
		}

	}

	@SuppressWarnings("unchecked")
	public <T extends ThumperStruct> T obj(Class<T> clazz) {
		// This is a 1 field enum struct
		if (clazz.isEnum()) {
			int count = 0;
			Type fieldType = null;
			for (var field : clazz.getFields()) {
				if (Modifier.isStatic(field.getModifiers())) continue;
				++count;
				fieldType = field.getType();
			}

			if (count != 1) {
				throw new IllegalStateException("enum has more than one non static field");
			}

			if (int.class.equals(fieldType)) {
				try {
					Method method = clazz.getMethod("fromValue", int.class);
					return (T) method.invoke(null, i32());
				} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}

		T instance = null;
		try {
			instance = clazz.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException e) {
			e.printStackTrace();
		}

		Objects.requireNonNull(instance);

		for (var field : clazz.getFields()) {
			if (Modifier.isStatic(field.getModifiers())) continue;
			var type = field.getType();

			try {
				if (boolean.class.equals(type)) field.setBoolean(instance, bool());
				else if (byte.class.equals(type)) field.setByte(instance, i8());
				else if (int.class.equals(type)) field.setInt(instance, i32());
				else if (float.class.equals(type)) field.setFloat(instance, f32());
				else if (String.class.equals(type)) field.set(instance, str());
				else if (byte[].class.equals(type))
					field.set(instance, i8arr(field.getAnnotation(FixedSize.class).count()));
				else if (int[].class.equals(type))
					field.set(instance, i32arr(field.getAnnotation(FixedSize.class).count()));
				else if (ThumperStruct.class.isAssignableFrom(type))
					field.set(instance, obj((Class<? extends ThumperStruct>) field.getType()));
				else if (List.class.isAssignableFrom(type)) {

					var genericType = field.getGenericType();

					if (genericType instanceof ParameterizedType pt) {
						var arg = pt.getActualTypeArguments()[0];

						if (String.class.equals(arg)) {
							field.set(instance, strlist());
						} else if (arg instanceof Class<?> c && ThumperStruct.class.isAssignableFrom(c)) {
							field.set(instance, objlist((Class<? extends ThumperStruct>) c));
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

		return instance;
	}

	public String cstr() {
		int start = pos;

		// find null terminator
		while (pos < bytes.length && bytes[pos] != 0) {
			pos++;
		}

		int length = pos - start;
		byte[] data = new byte[length];
		System.arraycopy(bytes, start, data, 0, length);
		pos++; // skip over null byte

		return new String(data);

	}
}
