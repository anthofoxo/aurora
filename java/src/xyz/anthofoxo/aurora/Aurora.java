package xyz.anthofoxo.aurora;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

import imgui.ImGui;
import imgui.app.Application;
import imgui.type.ImBoolean;
import xyz.anthofoxo.aurora.gui.Hasher;
import xyz.anthofoxo.aurora.gui.ModLauncher;
import xyz.anthofoxo.aurora.gui.ObjlibDecomp;
import xyz.anthofoxo.aurora.struct.PrecompiledBin;

public class Aurora {
	public static boolean integrated;
	public static boolean shouldLaunchThumper = false;
	public static boolean requestClose = false;
	public static ImBoolean showUserGuideOnStartup = new ImBoolean(UserConfig.shouldShowGuide());
	public static ImBoolean viewNewUserGuide = new ImBoolean(UserConfig.shouldShowGuide());

	private ImBoolean viewSettings = new ImBoolean();
	private ImBoolean demo = new ImBoolean();
	private Hasher hasher = new Hasher();
	private ObjlibDecomp objlibDecomp = new ObjlibDecomp();

	public Aurora() {
	}

	public void update(Application app) {
		if (requestClose) {
			glfwSetWindowShouldClose(app.getHandle(), true);
		}

		if (ImGui.beginMainMenuBar()) {

			if (ImGui.beginMenu("File")) {

				ImGui.menuItem("Preferences", null, viewSettings);

				if (ImGui.menuItem("Quit")) {
					glfwSetWindowShouldClose(app.getHandle(), true);
				}

				ImGui.endMenu();
			}

			if (ImGui.beginMenu("Tools")) {
				ImGui.menuItem("Hasher", null, hasher.visible);
				ImGui.menuItem("Objlib Decomp Tool", null, objlibDecomp.visible);
				ImGui.separator();
				ImGui.menuItem("Dear ImGui Demo", null, demo);

				ImGui.endMenu();
			}

			if (ImGui.beginMenu("Tests")) {
				if (ImGui.menuItem("Throw Exception")) {
					throw new RuntimeException("WHAT DID YOU THINK WAS GOING TO HAPPEN?????");
				}

				ImGui.endMenu();
			}

			if (ImGui.beginMenu("Help")) {

				ImGui.menuItem("New User Guide", null, viewNewUserGuide);

				ImGui.endMenu();
			}

			ImGui.endMainMenuBar();
		}

		if (viewSettings.get()) {
			if (ImGui.begin("Preferences", viewSettings)) {

				if (ImGui.smallButton("Choose New Path")) {
					UserConfig.properties.remove("thumper.path");
					UserConfig.thumperPath();
				}

				ImGui.sameLine();

				ImGui.textUnformatted(UserConfig.thumperPath());

				ImGui.separatorText("Mod Search Paths");

				int removeIdx = -1;

				for (int i = 0; i < UserConfig.modPaths.size(); ++i) {
					if (ImGui.smallButton("x")) removeIdx = i;
					ImGui.sameLine();
					ImGui.textUnformatted(UserConfig.modPaths.get(i));
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

			}
			ImGui.end();
		}

		if (demo.get()) {
			ImGui.showDemoWindow(demo);
		}

		if (viewNewUserGuide.get()) {
			if (ImGui.begin("New User Guide", viewNewUserGuide)) {
				ImGui.textWrapped(
						"Welcome to Aurora, the new and fully featured mod loader for Thumper. If this is your first time then thank you for trying out Aurora.");

				ImGui.separatorText("Speed Mofidier");
				ImGui.textWrapped(
						"Aurora allows you apply speed modifiers to any level, simply click on the custom level name and adjust the speed slider");

				ImGui.separator();

				ImGui.textWrapped(
						"If we find any problems we will have some messages below to help assist in solving these problems. If these are not enough guidance then ask for help via discord. We are always happy to help!");

				if (!Aurora.integrated) {
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
					ImGui.textWrapped(
							"You dont have any mod paths setup, aurora wont be able to find your custom levels!");
					ImGui.textWrapped(
							"Open the File > Preferences panel and add some search paths. You want to add the folder containing the levels. Not the level folder itself");
					if (ImGui.button("Open Preferences")) viewSettings.set(true);
				}

				ImGui.separator();

				if (ImGui.checkbox("Show User Guide on Startup", showUserGuideOnStartup)) {
					UserConfig.setShowGuide(showUserGuideOnStartup.get());
				}
			}
			ImGui.end();
		}

		ModLauncher.draw();
		hasher.draw();
		objlibDecomp.draw();
	}
}
