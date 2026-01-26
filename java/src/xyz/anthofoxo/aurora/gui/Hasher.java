package xyz.anthofoxo.aurora.gui;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import xyz.anthofoxo.aurora.Hash;

public class Hasher {
	public static String commExport;

	public ImBoolean visible = new ImBoolean();
	public ImString input = new ImString(512);
	public Integer hash;
	public String output;
	public boolean collision;

	public Hasher() {
		compute();
	}

	public void compute() {
		hash = Hash.fnv1a(Hash.escapedToByteArray(input.get()));
		output = Integer.toHexString(hash);
		collision = Hash.hashes.containsKey(hash);
	}

	public void draw() {
		if (!visible.get()) return;

		ImGui.setNextWindowSize(640, 480, ImGuiCond.FirstUseEver);
		if (ImGui.begin("Hasher", visible)) {
			if (ImGui.button("Reload Hashes")) {
				Hash.reloadHashes();
				compute();
			}

			if (ImGui.inputText("Input", input)) compute();
			ImGui.labelText("Output", output);

			if (collision) {
				ImGui.text("Hash found");

				if (ImGui.button("Open in ObjLib parse")) {
					commExport = output;
				}
			}
		}
		ImGui.end();
	}
}
