package xyz.anthofoxo.aurora.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import imgui.ImGui;
import imgui.type.ImString;
import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.UserConfig;
import xyz.anthofoxo.aurora.struct.AuroraReader;
import xyz.anthofoxo.aurora.struct.LocalizationFile;

public class LocalizationEditor {
	public LocalizationFile locs = new LocalizationFile();

	public LocalizationEditor() throws IOException {
		String path = String.format(UserConfig.thumperPath() + "/cache/%s.pc",
				Integer.toHexString(Hash.fnv1a("Aui/strings.en.loc")));

		AuroraReader in = new AuroraReader(Files.readAllBytes(Path.of(path)));

		locs.read(in);
	}

	private ImString buffer = new ImString(256);

	public void draw() {

		if (ImGui.begin("Localization Editor")) {
			for (var locs : locs.enteries) {
				buffer.set(locs.value);

				String label;
				byte[] hashTable = Hash.hashes.get(locs.key);

				if (hashTable != null) {
					label = new String(hashTable);
				} else {
					label = "0x" + Integer.toHexString(locs.key);
				}

				if (ImGui.inputText(label, buffer)) {
					locs.value = buffer.get();
				}

			}
		}
		ImGui.end();
	}
}
