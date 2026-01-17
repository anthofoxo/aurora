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
	public static final String TITLE = "Aurora v0.1.1";

	private GuiUserGuide userGuide = new GuiUserGuide();
	private ImBoolean demo = new ImBoolean();
	private Hasher hasher = new Hasher();
	private ObjlibDecomp objlibDecomp = new ObjlibDecomp();

	public static Map<String, Texture> icons = new HashMap<>();
	public static Map<String, Texture> buttonicons = new HashMap<>();
	public static Map<String, Texture> textures = new HashMap<>();

	public static void registerTexturesFromPath(Map<String, Texture> target, String path)
			throws URISyntaxException, IOException {
		for (var file : Util.getAllFilesFromResourceDirectory(path)) {
			target.put(file.getFileName().toString(), Texture.makeFromResource(file.toString()));
		}
	}

	public Aurora() {
		try {
			registerTexturesFromPath(icons, "difficulty_icons");
			registerTexturesFromPath(buttonicons, "button_icons");
			registerTexturesFromPath(textures, "textures");
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
	}

	public void update() {
		if (ImGui.beginMainMenuBar()) {

			if (ImGui.beginMenu("File")) {

				ImGui.menuItem("Preferences", null, GuiPreferences.visible);

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
		GuiPreferences.draw();
		userGuide.draw();
		ModLauncher.draw();
		hasher.draw();
		objlibDecomp.draw();

		drawBackgroundElement();
	}

	private void drawBackgroundElement() {
		var drawList = ImGui.getBackgroundDrawList();
		float viewportWidth = ImGui.getMainViewport().getSizeX();
		float viewportHeight = ImGui.getMainViewport().getSizeY();
		float size = Math.min(viewportWidth / 1.5f, viewportHeight / 1.5f);
		float margin = 64.0f;
		float x = viewportWidth - size - margin;
		float y = viewportHeight - size - margin;
		drawList.addImage(textures.get("aur_bg.png").getHandle(), x, y, x + size, y + size);
	}
}
