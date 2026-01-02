package xyz.anthofoxo.aurora.parse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import xyz.anthofoxo.aurora.struct.ThumperStruct;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.annotation.RemoveFieldIfEnclosed;

public class AuroraReader {
	public byte[] bytes;
	public int pos;
	public Stack<Class<?>> enclosing = new Stack<>();

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

	public long i64() {
		long b0 = i8() & 0xFFL;
		long b1 = i8() & 0xFFL;
		long b2 = i8() & 0xFFL;
		long b3 = i8() & 0xFFL;
		long b4 = i8() & 0xFFL;
		long b5 = i8() & 0xFFL;
		long b6 = i8() & 0xFFL;
		long b7 = i8() & 0xFFL;

		return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24) | (b4 << 32) | (b5 << 40) | (b6 << 48) | (b7 << 56);
	}

	public Instant instant() {
		return Instant.ofEpochSecond(i64());
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

	public float[] f32arr(int count) {
		float[] values = new float[count];
		for (int i = 0; i < count; ++i) values[i] = f32();
		return values;
	}

	public List<String> strlist() {
		int count = i32();
		List<String> values = new ArrayList<String>();
		for (int i = 0; i < count; ++i) values.add(str());
		return values;
	}

	public <T extends ThumperStruct> List<T> objlist(Class<T> clazz) {
		int count = i32();

		List<T> values = new ArrayList<T>();
		for (int i = 0; i < count; ++i) values.add(obj(clazz));
		return values;

	}

	@SuppressWarnings("unchecked")
	public <T extends ThumperStruct> T obj(Class<T> clazz) {

		enclosing.push(clazz);

		try {
			// Does class provide custom override?, if so then invoke it
			try {
				var method = clazz.getMethod("in", AuroraReader.class);

				try {
					return clazz.cast(method.invoke(null, this));
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			} catch (NoSuchMethodException e) {
			}

			T instance = null;
			try {
				instance = clazz.getConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}

			Objects.requireNonNull(instance);

			nextfield: for (var field : clazz.getFields()) {
				if (Modifier.isStatic(field.getModifiers())) continue;
				var type = field.getType();

				var removalAnnotation = field.getAnnotation(RemoveFieldIfEnclosed.class);
				if (removalAnnotation != null) {
					boolean ignoreField = false;

					for (var itCtx : enclosing) {
						if (itCtx.equals(removalAnnotation.clazz())) {
							ignoreField = true;
							break;
						}

					}

					if (ignoreField) {
						// If this field shouldnt be written then do not write it
						continue nextfield;
					}
				}

				try {
					if (boolean.class.equals(type)) field.setBoolean(instance, bool());
					else if (byte.class.equals(type)) field.setByte(instance, i8());
					else if (int.class.equals(type)) field.setInt(instance, i32());
					else if (float.class.equals(type)) field.setFloat(instance, f32());
					else if (long.class.equals(type)) field.setLong(instance, i64());
					else if (String.class.equals(type)) field.set(instance, str());
					else if (Instant.class.equals(type)) field.set(instance, instant());

					else if (byte[].class.equals(type))
						field.set(instance, i8arr(field.getAnnotation(FixedSize.class).count()));
					else if (float[].class.equals(type))
						field.set(instance, f32arr(field.getAnnotation(FixedSize.class).count()));
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
						throw new IllegalStateException("Failed to parse " + field.toString());
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}

			return instance;

		} finally {
			enclosing.pop();
		}

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

	public int position() {
		return pos;
	}

	public byte[] i8remaining() {
		int numRemainingBytes = bytes.length - pos;
		return i8arr(numRemainingBytes);
	}

}
