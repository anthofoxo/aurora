package xyz.anthofoxo.aurora.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImBoolean;
import xyz.anthofoxo.aurora.TextureRegistry;
import xyz.anthofoxo.aurora.UserConfig;
import xyz.anthofoxo.aurora.gfx.Font;

public final class GuiPreferences {
	private GuiPreferences() {
	}

	public static ImBoolean visible = new ImBoolean(false);
	public static ImBoolean unlockPractice = new ImBoolean(UserConfig.isUnlockPractice());

	public static void modSearchPathsPanel() {
		if (ImGui.button("Add Search Path")) {
			String path = TinyFileDialogs.tinyfd_selectFolderDialog("Mod Search Path", null);

			if (path != null) {
				UserConfig.modPaths.add(path);
				UserConfig.save();
				ModLauncher.reloadList();
			}
		}

		ImGui.sameLine();
		ImGui.pushFont(Font.getFont("defaultsmall"));
		ImGui.text("Any levels found in the listed paths will appear in the level list.");
		ImGui.popFont();

		int removeIdx = -1;

		for (int i = 0; i < UserConfig.modPaths.size(); ++i) {
			ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, new ImVec2(3f, 3f));
			var buttonremove = TextureRegistry.get("button_icons/icon-remove.png");
			ImGui.pushID(i);
			ImGui.pushStyleColor(ImGuiCol.Button, new ImVec4(0.4f, 0, 0, 1));
			ImGui.pushStyleColor(ImGuiCol.ButtonHovered, new ImVec4(1f, 0, 0, 1));
			if (ImGui.imageButton("buttonremove", buttonremove.getHandle(), 16, 16)) removeIdx = i;
			ImGui.popStyleColor(2);

			ImGui.sameLine();

			var buttonfolder = TextureRegistry.get("button_icons/icon-openfolder.png");
			if (ImGui.imageButton("buttonfolder", buttonfolder.getHandle(), 16, 16)) {
				try {
					Desktop.getDesktop().open(new File(UserConfig.modPaths.get(i)));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			ImGui.popStyleVar();

			ImGui.sameLine();
			ImGui.pushFont(Font.getFont("consolas"));
			ImGui.textUnformatted(UserConfig.modPaths.get(i));
			ImGui.popFont();
			ImGui.popID();
		}

		if (removeIdx != -1) {
			UserConfig.modPaths.remove(removeIdx);
			UserConfig.save();
			ModLauncher.reloadList();
		}
	}

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

		modSearchPathsPanel();

		ImGui.end();
	}
}
