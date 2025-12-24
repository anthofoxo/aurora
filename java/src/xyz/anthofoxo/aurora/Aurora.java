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
import xyz.anthofoxo.aurora.gui.LocalizationEditor;
import xyz.anthofoxo.aurora.tml.TMLLevel;

public class Aurora {
	public static boolean integrated;
	public static boolean shouldLaunchThumper = false;
	public static String home = "C:/Program Files (x86)/Steam/steamapps/common/Thumper";

	private ImBoolean demo = new ImBoolean();
	private Hasher hasher = new Hasher();
	private LocalizationEditor locEditor;

	public Aurora() {
		try {
			locEditor = new LocalizationEditor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			TMLLevel.test();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void update(Application app) {
		if (ImGui.beginMainMenuBar()) {

			if (ImGui.beginMenu("File")) {

				if(ImGui.menuItem("Launch Thumper")) {
					shouldLaunchThumper = true;
					glfwSetWindowShouldClose(app.getHandle(), true);
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
		locEditor.draw();
	}
}
