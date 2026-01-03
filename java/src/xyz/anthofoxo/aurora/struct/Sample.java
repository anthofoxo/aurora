package xyz.anthofoxo.aurora.struct;

import java.util.List;

import imgui.ImGui;
import imgui.type.ImString;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class Sample implements ThumperStruct {
	public static int[] header() {
		return new int[] { 12, 4 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	public String mode;
	public int unknown0;
	public String path;
	@FixedSize(count = 5)
	public byte[] unknown1;
	public float volume;
	public float pitch;
	public float pan;
	public float offset;
	public String channelGroup;

	@Override
	public String toString() {
		return path;
	}

	private static ImString tmpStr = new ImString(256);
	private static float[] tmpFloat = new float[1];

	public void gui() {

		tmpStr.set(path);
		if (ImGui.inputText("Path", tmpStr)) path = tmpStr.get();

		tmpStr.set(mode);
		if (ImGui.inputText("Mode", tmpStr)) mode = tmpStr.get();

		tmpStr.set(channelGroup);
		if (ImGui.inputText("Channel Group", tmpStr)) channelGroup = tmpStr.get();

		tmpFloat[0] = volume;
		if (ImGui.sliderFloat("Volume", tmpFloat, 0, 1)) volume = tmpFloat[0];

		tmpFloat[0] = pitch;
		if (ImGui.sliderFloat("Pitch", tmpFloat, 0, 5)) pitch = tmpFloat[0];

		tmpFloat[0] = pan;
		if (ImGui.sliderFloat("Pan", tmpFloat, -1, 1)) pan = tmpFloat[0];

		tmpFloat[0] = offset;
		if (ImGui.sliderFloat("Offset", tmpFloat, -1, 1)) offset = tmpFloat[0];

	}
}