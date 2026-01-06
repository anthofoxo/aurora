package xyz.anthofoxo.aurora;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.lwjgl.glfw.GLFW;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiPopupFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import xyz.anthofoxo.aurora.gui.GuiPreferences;
import xyz.anthofoxo.aurora.gui.GuiUserGuide;
import xyz.anthofoxo.aurora.gui.Hasher;
import xyz.anthofoxo.aurora.gui.ModLauncher;
import xyz.anthofoxo.aurora.gui.ObjlibDecomp;
import xyz.anthofoxo.aurora.target.Target;

public class Aurora {
	public static final String TITLE = "Aurora v0.2.0-a.4+WIP";

	public static boolean hasSessionLock = false;

	private GuiUserGuide userGuide = new GuiUserGuide();
	private ImBoolean demo = new ImBoolean();
	private Hasher hasher = new Hasher();
	private ObjlibDecomp objlibDecomp = new ObjlibDecomp();

	public Aurora() {
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

		// We are building, ensure the popup is visible
		if (buildProgress != null) {
			// if (!ImGui.isPopupOpen("aur_building", ImGuiPopupFlags.AnyPopupId)) {
			ImGui.openPopup("aur_building");
			// }
		}

		if (ImGui.isPopupOpen("aur_building", ImGuiPopupFlags.AnyPopupId)) {
			ImGui.setNextWindowPos(ImGui.getMainViewport().getCenterX(), ImGui.getMainViewport().getCenterY(),
					ImGuiCond.Appearing, 0.5f, 0.5f);
		}

		if (ImGui.beginPopupModal("aur_building", ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoTitleBar)) {
			if (!buildProgress.isDone()) {
				ImGui.text("Building targets...");
				ImGui.progressBar(-1.0f * (float) GLFW.glfwGetTime());
			} else {

				if (buildText != null) {
					ImGui.text("Targets Failed, Thumper cache may be in an invalid state");
					ImGui.separator();
					ImGui.text(buildText);

					if (ImGui.button("Copy Error Message")) {
						ImGui.setClipboardText(buildText);
					}
					ImGui.sameLine();
				} else {
					ImGui.text("Targets Built Successfully");

					if (AuroraStub.integrated) {
						EntryPoint.running = false;
						AuroraStub.shouldLaunchThumper = true;
					}

				}

				if (ImGui.button("Close")) {
					ImGui.closeCurrentPopup();
					buildProgress = null;

				}
			}

			ImGui.endPopup();
		}

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

	private static String buildText;
	private static CompletableFuture<Void> buildProgress;

	private static CompletableFuture<Void> buildModsAsync(List<Target> targets, boolean modModeEnabled,
			boolean unlockLevels) {
		return CompletableFuture.runAsync(() -> {
			try {
				buildText = null;
				ModBuilder.build(targets, modModeEnabled, unlockLevels);
			} catch (Throwable e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				buildText = sw.toString();
			}
		});
	}

	public static void buildAndLaunch(List<Target> targets, boolean modModeEnabled, boolean unlockLevels) {
		buildProgress = buildModsAsync(targets, modModeEnabled, unlockLevels);
	}
}
