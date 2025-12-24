package xyz.anthofoxo.aurora;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import java.io.IOException;

import imgui.ImGui;
import imgui.app.Application;
import imgui.type.ImBoolean;
import xyz.anthofoxo.aurora.gui.Hasher;
import xyz.anthofoxo.aurora.gui.LocalizationEditor;
import xyz.anthofoxo.aurora.gui.ModLauncher;

public class Aurora {
	public static boolean integrated;
	public static boolean shouldLaunchThumper = false;
	public static boolean requestClose = false;

	private ImBoolean demo = new ImBoolean();
	private Hasher hasher = new Hasher();
	private LocalizationEditor locEditor;
	private ModLauncher modLauncher = new ModLauncher();

	public Aurora() {
		try {
			locEditor = new LocalizationEditor();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void update(Application app) {
		if (requestClose) {
			glfwSetWindowShouldClose(app.getHandle(), true);
		}

		if (ImGui.beginMainMenuBar()) {

			if (ImGui.beginMenu("File")) {

				if (ImGui.menuItem("Launch Thumper")) {
					shouldLaunchThumper = true;
					requestClose = true;
				}

				if (ImGui.menuItem("Quit")) {
					glfwSetWindowShouldClose(app.getHandle(), true);
				}

				ImGui.endMenu();
			}

			if (ImGui.beginMenu("Tools")) {
				ImGui.menuItem("Hasher", null, hasher.visible);
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

		if (demo.get()) {
			ImGui.showDemoWindow(demo);
		}

		if (ImGui.begin("Aurora Information")) {
			if (!integrated) {
				ImGui.text("Aurora is running in standalone mode. Some features wil be disabled");
			} else {
				ImGui.text("Aurora is running in integrated mode. All features are enabled");
			}
		}
		ImGui.end();

		modLauncher.draw();
		hasher.draw();
		locEditor.draw();
	}
}
