package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.comp.Comp;

public class SequinDrawer {
	public static int[] header() {
		return new int[] { 7, 4 };
	}

	public int[] header;

	/**
	 * This is technically a list of comps But SequinDrawer is only ever seen with
	 * DrawComp
	 */
	public List<Comp> comps;
}
