package xyz.anthofoxo.aurora.gui;

import imgui.ImGui;
import imgui.type.ImBoolean;

public final class GuiCredits {
	private GuiCredits() {
	}

	public static ImBoolean visible = new ImBoolean(false);

	public static void draw() {
		if (!visible.get()) return;

		if (!ImGui.begin("Credits", visible)) {
			ImGui.end();
			return;
		}

		ImGui.textUnformatted("Lead Developer");
		ImGui.bulletText("AnthoFoxo");

		ImGui.textUnformatted("UX/UI Designer");
		ImGui.bulletText("CocoaMix");

		ImGui.textUnformatted("Reverse Engineering");
		ImGui.bulletText("AnthoFoxo");
		ImGui.bulletText("JLMusic");
		ImGui.bulletText("NotAFacist");

		ImGui.textUnformatted("Alpha and Beta Testing");
		ImGui.bulletText("JLMusic");

		ImGui.textUnformatted("Artwork");
		ImGui.bulletText("Pentaria");

		ImGui.textUnformatted("Thumper");
		ImGui.bulletText("Drool LLC.");

		ImGui.end();
	}
}
