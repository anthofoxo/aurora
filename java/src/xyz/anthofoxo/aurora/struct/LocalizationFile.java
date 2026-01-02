package xyz.anthofoxo.aurora.struct;

import java.util.ArrayList;
import java.util.List;

import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.parse.AuroraWriter;

public class LocalizationFile {
	public static class Entry {
		public String value;
		public int key;

		public Entry() {
		}

		public Entry(String value, int key) {
			this.value = value;
			this.key = key;
		}

		@Override
		public String toString() {
			return String.format("%s(%s)", value, Integer.toHexString(key));
		}
	}

	public int fileType = 6;
	public List<Entry> enteries;

	public boolean containsKey(int key) {
		for (var entry : enteries) {
			if (entry.key == key) return true;
		}
		return false;
	}

	public int indexOfKey(int key) {
		for (int i = 0; i < enteries.size(); ++i) {
			var entry = enteries.get(i);
			if (entry.key == key) return i;
		}
		return -1;
	}

	public void read(AuroraReader in) {
		enteries = new ArrayList<>();
		fileType = in.i32();
		int cstrCount = in.i32();
		in.i32(); // byte count, we dont care about this for reading

		for (int i = 0; i < cstrCount; ++i) {
			Entry e = new Entry();
			e.value = in.cstr();
			enteries.add(e);
		}

		for (int i = 0; i < cstrCount; ++i) {
			int key = in.i32();
			in.i32(); // byte offset, we dont care about this for reading
			enteries.get(i).key = key;
		}
	}

	public void write(AuroraWriter out) {
		out.i32(fileType);

		int bytes = 0;
		for (var entry : enteries) bytes += entry.value.getBytes().length + 1;
		out.i32(enteries.size());
		out.i32(bytes);
		for (var entry : enteries) out.cstr(entry.value);
		int offset = 0;
		for (var entry : enteries) {
			out.i32(entry.key);
			out.i32(offset);
			offset += entry.value.getBytes().length + 1;
		}

	}
}