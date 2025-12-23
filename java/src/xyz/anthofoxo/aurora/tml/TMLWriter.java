package xyz.anthofoxo.aurora.tml;

import java.util.ArrayList;

import tools.jackson.databind.node.ArrayNode;
import xyz.anthofoxo.aurora.Hash;

public class TMLWriter {
	public ArrayList<Byte> bytes = new ArrayList<>();

	public void bool(boolean v) {
		i8(v ? (byte) 1 : (byte) 0);
	}

	public void i8(byte v) {
		bytes.add(v);
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

	private static byte[] stringToByteArray(String hex) {
		int len = hex.length();
		byte[] bytes = new byte[len / 2];

		for (int i = 0; i < len; i += 2) {
			bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
		}

		return bytes;
	}

	public void i8arr(String v) {
		i8arr(stringToByteArray(v));
	}

	public void i8arr(byte[] v) {
		for (var e : v) i8(e);
	}

	public void i8arrReverse(String v) {
		var bytes = stringToByteArray(v);
		for (int i = bytes.length - 1; i >= 0; --i) i8(bytes[i]);
	}

	public void hash(String v) {
		i32(Hash.fnv1a(v));
	}

	public void vec3(ArrayNode jsonNode) {
		f32(jsonNode.get(0).asFloat());
		f32(jsonNode.get(1).asFloat());
		f32(jsonNode.get(2).asFloat());
	}

	public byte[] toBytes() {
		byte[] bytes = new byte[this.bytes.size()];
		int offset = 0;

		for (var b : this.bytes) {
			bytes[offset++] = b;
		}

		return bytes;
	}

}
