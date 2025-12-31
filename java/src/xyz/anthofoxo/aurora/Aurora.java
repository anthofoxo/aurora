package xyz.anthofoxo.aurora;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import imgui.ImGui;
import imgui.type.ImBoolean;
import xyz.anthofoxo.aurora.gfx.Texture;
import xyz.anthofoxo.aurora.gui.GuiPreferences;
import xyz.anthofoxo.aurora.gui.GuiUserGuide;
import xyz.anthofoxo.aurora.gui.Hasher;
import xyz.anthofoxo.aurora.gui.ModLauncher;
import xyz.anthofoxo.aurora.gui.ObjlibDecomp;

public class Aurora {
	public static final String TITLE = "Aurora v0.2.0-a.1+WIP";

	private GuiUserGuide userGuide = new GuiUserGuide();
	private GuiPreferences preferences = new GuiPreferences();
	private ImBoolean demo = new ImBoolean();
	private Hasher hasher = new Hasher();
	private ObjlibDecomp objlibDecomp = new ObjlibDecomp();

	public static Map<String, Texture> icons = new HashMap<>();

	public Aurora() {

		try {
			var list = Util.getAllFilesFromResourceDirectory("difficulty_icons");

			for (var item : list) {
				icons.put(item.getFileName().toString(), Texture.makeFromResource(item.toString()));
			}
		} catch (URISyntaxException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void update() {
		if (ImGui.beginMainMenuBar()) {

			if (ImGui.beginMenu("File")) {

				ImGui.menuItem("Preferences", null, preferences.visible);

				if (ImGui.menuItem("Quit")) {
					EntryPoint.running = false;
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

				if (ImGui.menuItem("Thumper / Aurora Documentation")) {
					Util.openURL("https://anthofoxo.xyz/aurora/");
				}

				ImGui.menuItem("New User Guide", null, userGuide.visible);

				ImGui.endMenu();
			}

			ImGui.endMainMenuBar();
		}

		if (demo.get()) ImGui.showDemoWindow(demo);
		preferences.draw();
		userGuide.draw(preferences);
		ModLauncher.draw();
		hasher.draw();
		objlibDecomp.draw();
	}
}
