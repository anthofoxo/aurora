package xyz.anthofoxo.aurora.struct;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.parse.AuroraReader;

public enum LibraryType implements ThumperStruct {
	LevelLib("LevelLib"), GfxLib("GfxLib"), AvatarLib("AvatarLib"), SequinLib("SequinLib"), ObjLib("ObjLib");

	public int value;

	private LibraryType(String hash) {
		this.value = Hash.fnv1a(hash);
	}

	public static LibraryType in(AuroraReader in) {
		int hash = in.i32();

		for (var v : values()) {
			if (hash == v.value) return v;
		}

		throw new IllegalStateException("No library type has the value: " + Integer.toHexString(hash));
	}
}
