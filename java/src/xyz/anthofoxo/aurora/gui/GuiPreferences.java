package xyz.anthofoxo.aurora.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

import imgui.ImGui;
import imgui.type.ImBoolean;
import xyz.anthofoxo.aurora.UserConfig;

public final class GuiPreferences {
	private GuiPreferences() {
	}

	public static ImBoolean visible = new ImBoolean(false);
	public static ImBoolean unlockPractice = new ImBoolean(UserConfig.isUnlockPractice());

	public static void draw() {
		if (!visible.get()) return;

		if (!ImGui.begin("Preferences", visible)) {
			ImGui.end();
		}

		if (ImGui.checkbox("Unlock Practice Mode", unlockPractice)) {
			UserConfig.setUnlockPractice(unlockPractice.get());
		}

		ImGui.separator();

		if (ImGui.smallButton("Choose New Path")) {
			UserConfig.properties.remove("thumper.path");
			UserConfig.thumperPath();
		}

		ImGui.sameLine();

		ImGui.textUnformatted(UserConfig.thumperPath());

		ImGui.separatorText("Mod Search Paths");

		int removeIdx = -1;

		for (int i = 0; i < UserConfig.modPaths.size(); ++i) {
			ImGui.pushID(i);
			if (ImGui.smallButton("x")) removeIdx = i;

			ImGui.sameLine();

			if (ImGui.smallButton("Open")) {
				try {
					Desktop.getDesktop().open(new File(UserConfig.modPaths.get(i)));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			ImGui.sameLine();
			ImGui.textUnformatted(UserConfig.modPaths.get(i));
			ImGui.popID();
		}

		if (removeIdx != -1) {
			UserConfig.modPaths.remove(removeIdx);
			UserConfig.save();
			ModLauncher.reloadList();
		}

		if (ImGui.button("Add Search Path")) {
			String path = TinyFileDialogs.tinyfd_selectFolderDialog("Mod Search Path", null);

			if (path != null) {
				UserConfig.modPaths.add(path);
				UserConfig.save();
				ModLauncher.reloadList();
			}

		}

		ImGui.end();
	}
}
