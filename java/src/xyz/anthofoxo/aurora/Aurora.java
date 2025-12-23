package xyz.anthofoxo.aurora;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import imgui.ImGui;
import imgui.app.Application;
import imgui.type.ImBoolean;
import xyz.anthofoxo.aurora.gui.Hasher;

public class Aurora {
	public static boolean standalone;
	public static String home = "C:/Program Files (x86)/Steam/steamapps/common/Thumper";

	private ImBoolean demo = new ImBoolean();
	private Hasher hasher = new Hasher();

	public void update(Application app) {
		if (ImGui.beginMainMenuBar()) {

			if (ImGui.beginMenu("File")) {

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
			if (standalone) {
				ImGui.text("Aurora is running in standalone mode. Some features wil be disabled");
			} else {
				ImGui.text("Aurora is running in integrated mode. All features are enabled");
			}
		}
		ImGui.end();

		if (ImGui.begin("Debug")) {

			try (DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(home))) {
				for (Path entry : stream) {
					ImGui.text(entry.getFileName().toString());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		ImGui.end();

		hasher.draw();
	}
}
