package xyz.anthofoxo.aurora.gui;

import imgui.ImGui;
import imgui.type.ImBoolean;
import xyz.anthofoxo.aurora.AuroraStub;
import xyz.anthofoxo.aurora.UserConfig;

public class GuiUserGuide {
	public static ImBoolean showUserGuideOnStartup = new ImBoolean(UserConfig.shouldShowGuide());
	public ImBoolean visible = new ImBoolean(showUserGuideOnStartup);

	public void draw() {
		if (!visible.get()) return;

		if (!ImGui.begin("New User Guide", visible)) {
			ImGui.end();
			return;
		}

		ImGui.textWrapped(
				"Welcome to Aurora, the new and fully featured mod loader for Thumper. If this is your first time then thank you for trying out Aurora.");

		ImGui.separatorText("Speed Mofidier");
		ImGui.textWrapped(
				"Aurora allows you apply speed modifiers to any level, simply click on the custom level name and adjust the speed slider");

		ImGui.separator();

		ImGui.textWrapped(
				"If we find any problems we will have some messages below to help assist in solving these problems. If these are not enough guidance then ask for help via discord. We are always happy to help!");

		if (!AuroraStub.integrated) {
			ImGui.separatorText("Aurora is running standalone instead of integrated");
			ImGui.textWrapped(
					"It seems you may have had trouble getting Aurora to run nativly with Thumper. Here's some tips to set that up.");
			ImGui.bulletText(
					"In the .zip you download there are two files: this .jar file, and a steam_api64.dll file");
			ImGui.bulletText("Using steam browse the local files for Thumper");
			ImGui.bulletText(
					"Make sure a steam_api64.dll.bak exists. If not make a copy of steam_api64.dll and give it this name \"steam_api64.dll.bak\"");
			ImGui.bulletText(
					"Copy the contents of the .zip package directly next to THUMPER_win8.exe, confirm the file overwrite");
			ImGui.bulletText(
					"At this point aurora is integrated into thumper, You can simply launch thumper via steam to get the full feature set");
		}

		if (UserConfig.modPaths.isEmpty()) {
			ImGui.separatorText("Levels cannot be found");
			ImGui.textWrapped("You dont have any mod paths setup, aurora wont be able to find your custom levels!");
			ImGui.textWrapped(
					"Open the File > Preferences panel and add some search paths. You want to add the folder containing the levels. Not the level folder itself");
			if (ImGui.button("Open Preferences")) GuiPreferences.visible.set(true);
		}

		ImGui.separator();

		if (ImGui.checkbox("Show User Guide on Startup", showUserGuideOnStartup)) {
			UserConfig.setShowGuide(showUserGuideOnStartup.get());
		}

		ImGui.end();
	}
}
