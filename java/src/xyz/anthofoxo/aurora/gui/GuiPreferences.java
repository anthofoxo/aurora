package xyz.anthofoxo.aurora.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.lwjgl.openal.AL11;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImBoolean;
import xyz.anthofoxo.aurora.TextureRegistry;
import xyz.anthofoxo.aurora.UserConfig;
import xyz.anthofoxo.aurora.Util;
import xyz.anthofoxo.aurora.gfx.Font;

public final class GuiPreferences {
	private GuiPreferences() {
	}

	public static ImBoolean visible = new ImBoolean(false);
	public static ImBoolean unlockPractice = new ImBoolean(UserConfig.isUnlockPractice());
	public static ImBoolean announcePrereleases = new ImBoolean(UserConfig.get("aurora.announce.prerelease", false));

	public static void modSearchPathPanel() {

		if (ImGui.button("Add Search Path")) {

			try {
				SwingUtilities.invokeAndWait(() -> {
					var chooser = new JFileChooser();
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.requestFocus();

					if (chooser.showOpenDialog(Util.makeOnTopParent()) == JFileChooser.APPROVE_OPTION) {
						UserConfig.modPaths.add(chooser.getSelectedFile().toString());
						UserConfig.save();
						ModLauncher.reloadList();
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

	public static void thumperPathPanel() {
		if (ImGui.button("Thumper Game Install Location")) {
			UserConfig.pickAndSaveThumperPath();
		}

		ImGui.sameLine();

		ImGui.textUnformatted(UserConfig.thumperPath());
	}

	public static void draw() {
		if (!visible.get()) return;

		if (!ImGui.begin("Preferences", visible)) {
			ImGui.end();
		}

		if (ImGui.checkbox("Unlock Practice Mode", unlockPractice)) {
			UserConfig.setUnlockPractice(unlockPractice.get());
		}

		if (ImGui.checkbox("Annouce Pre-release Builds", announcePrereleases)) {
			UserConfig.set("aurora.announce.prerelease", unlockPractice.get());
		}

		float[] volume = new float[] { Float.parseFloat(UserConfig.get("aurora.audio.master", String.valueOf(1.0f))) };
		if (ImGui.sliderFloat("Master Volume", volume, 0.0f, 1.0f)) {
			UserConfig.set("aurora.audio.master", Float.toString(volume[0]));
			AL11.alListenerf(AL11.AL_GAIN, volume[0]);
		}

		ImGui.separator();

		thumperPathPanel();

		ImGui.separator();

		ImGui.separatorText("Mod Search Paths");
		modSearchPathPanel();

		ImGui.end();
	}
}
