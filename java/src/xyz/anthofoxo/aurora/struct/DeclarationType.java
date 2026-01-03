package xyz.anthofoxo.aurora.struct;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.parse.AuroraReader;

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
	SequinPulse("SequinPulse"),
	Cam("Cam"),
	Scene("Scene"),
	VrSettings("VrSettings"),
	_DCH(0xac1abb2c),
	_Vib(0x799c45a7),
	_Pass(0x4e1efaa5),
	_Pp(0x7c85d725);
	// @formatter:on

	public final int value;

	private DeclarationType(String name) {
		this.value = Hash.fnv1a(name);
	}

	private DeclarationType(int value) {
		this.value = value;
	}

	public static DeclarationType in(AuroraReader in) {
		int hash = in.i32();

		for (var v : values()) {
			if (hash == v.value) return v;
		}

		throw new IllegalStateException("No declaration type has the value: " + Integer.toHexString(hash));
	}
}
