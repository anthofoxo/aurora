package xyz.anthofoxo.aurora;

import imgui.ImGui;
import imgui.type.ImBoolean;
import xyz.anthofoxo.aurora.gui.GuiCredits;
import xyz.anthofoxo.aurora.gui.GuiPreferences;
import xyz.anthofoxo.aurora.gui.GuiUserGuide;
import xyz.anthofoxo.aurora.gui.Hasher;
import xyz.anthofoxo.aurora.gui.ModLauncher;
import xyz.anthofoxo.aurora.gui.ObjlibDecomp;

public class Aurora {
	public static final String TAG = "v0.2.0-rc.1";
	public static final String TITLE = "Aurora " + TAG;

	public static boolean hasSessionLock = false;

	private GuiUserGuide userGuide = new GuiUserGuide();
	private ImBoolean demo = new ImBoolean();
	private Hasher hasher = new Hasher();
	private ObjlibDecomp objlibDecomp = new ObjlibDecomp();

	public Aurora() {
		// On startup if the path isnt set, prompt the user to set it
		if (UserConfig.thumperPath() == null) UserConfig.pickAndSaveThumperPath();

		Updater.announceReleases();
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
				if (ImGui.menuItem("Audio Sample Dump")) AudioExtract.open();

				ImGui.menuItem("Objlib Decomp Tool", null, objlibDecomp.visible);
				ImGui.separator();
				ImGui.menuItem("Dear ImGui Demo", null, demo);

				ImGui.endMenu();
			}

			if (ImGui.beginMenu("Help")) {

				if (ImGui.menuItem("Thumper / Aurora Documentation")) {
					Util.openURL("https://anthofoxo.xyz/aurora/");
				}

				ImGui.menuItem("New User Guide", null, userGuide.visible);

				ImGui.separator();

				ImGui.menuItem("Credits", null, GuiCredits.visible);

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

		ModBuilder.gui();
		GuiCredits.draw();
		AudioExtract.draw();

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
		drawList.addImage(TextureRegistry.get("aur_bg.png").getHandle(), x, y, x + size, y + size);
	}
}
