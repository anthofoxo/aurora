package xyz.anthofoxo.aurora.struct.comp;

import xyz.anthofoxo.aurora.Hash;

public class DrawComp implements Comp {
	/**
	 * LE: EE 19 27 F9 <br>
	 * BE: F9 27 19 EE
	 */
	public static final int HASH = Hash.fnv1a("DrawComp");

	public int hash = HASH;
	public int unknown0 = 8; // might be a kTraitAction which expects a boolean after?
	public boolean unknown1 = true; // visible?
	public String drawLayers = "kNumDrawLayers";
	public String parent = "kBucketParent";
	
	public DrawComp(int unknown0, boolean unknown1, String drawLayers, String parent) {
		this.unknown0 = unknown0;
		this.unknown1 = unknown1;
		this.drawLayers = drawLayers;
		this.parent = parent;
	}
	
	
}
