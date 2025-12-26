package xyz.anthofoxo.aurora;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

import imgui.ImGui;
import imgui.app.Application;
import imgui.type.ImBoolean;
import xyz.anthofoxo.aurora.gui.Hasher;
import xyz.anthofoxo.aurora.gui.ModLauncher;
import xyz.anthofoxo.aurora.gui.ObjlibDecomp;

public class Aurora {
	public static boolean integrated;
	public static boolean shouldLaunchThumper = false;
	public static boolean requestClose = false;

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

		ModLauncher.draw();
		hasher.draw();
		objlibDecomp.draw();
	}
}
