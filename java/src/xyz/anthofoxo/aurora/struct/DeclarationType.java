package xyz.anthofoxo.aurora.struct;

import xyz.anthofoxo.aurora.Hash;

public enum DeclarationType implements ThumperStruct {
	// @formatter:off
	SequinMaster("SequinMaster"),
	SequinLevel("SequinLevel"),
	SequinGate("SequinGate"),
	SequinLeaf("SequinLeaf"),
	EntitySpawner("EntitySpawner"),
	SequinDrawer("SequinDrawer"),
	Sample("Sample"),
	Path("Path"),
	Mesh("Mesh"),
	Flow("Flow"),
	Mat("Mat"),
	DSP("DSP"),
	Xfmer("Xfmer"),
	Tex2D("Tex2D"),
	Env("Env"),
	ChannelGroup("ChannelGroup"),
	PathDecorator("PathDecorator"),
	TraitAnim("TraitAnim"),
	Bender("Bender"),
	SequinPulse("SequinPulse");
	// @formatter:on

	public final int value;

	private DeclarationType(String name) {
		this.value = Hash.fnv1a(name);
	}

	private DeclarationType(int value) {
		this.value = value;
	}
}
