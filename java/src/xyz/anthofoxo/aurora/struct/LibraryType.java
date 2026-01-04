package xyz.anthofoxo.aurora.struct;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.parse.AuroraReader;

public enum LibraryType implements ThumperStruct {
	/**
	 * LE: 9e 4d 37 0b
	 */
	LevelLib("LevelLib"),

	/**
	 * LE: 43 14 a5 1b
	 */
	GfxLib("GfxLib");

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
