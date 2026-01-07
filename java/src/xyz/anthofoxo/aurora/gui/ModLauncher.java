package xyz.anthofoxo.aurora.gui;

import java.util.ArrayList;
import java.util.List;

import imgui.ImGui;
import imgui.ImGuiTextFilter;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import xyz.anthofoxo.aurora.AuroraStub;
import xyz.anthofoxo.aurora.BuiltinModOptions;
import xyz.anthofoxo.aurora.ModBuilder;
import xyz.anthofoxo.aurora.TextureRegistry;
import xyz.anthofoxo.aurora.UserConfig;
import xyz.anthofoxo.aurora.gfx.Font;
import xyz.anthofoxo.aurora.target.Target;

public class ModLauncher {

	private ModLauncher() {

	}

	private static final String USERCONFIG_NATIVE_GAME_STR = "aurora.enable_campaign";

	private static List<Target> customs = new ArrayList<>();
	private static Target selected = null;
	private static ImBoolean buildTargets = new ImBoolean(true);
	private static ImBoolean isModModeEnabled = new ImBoolean(true);
	private static ImGuiTextFilter filter = new ImGuiTextFilter();
	private static ImBoolean autoUnlockLevels = new ImBoolean(true);

	private static ImBoolean enableCampaignLevels = new ImBoolean(
			Boolean.parseBoolean(UserConfig.get(USERCONFIG_NATIVE_GAME_STR, Boolean.toString(true))));

	private static boolean showselected = false;
	private static boolean ranksortorder = false;
	private static boolean namesortorder = false;

	static {
		reloadList();
	}

	public static void reloadList() {
		ModBuilder.reloadTargetList(customs, enableCampaignLevels.get());
		selected = null;
	}

	public static void draw() {
		ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 6);
		if (ImGui.begin("Launcher", ImGuiWindowFlags.MenuBar)) {
			if (ImGui.beginMenuBar()) {
				if (ImGui.beginMenu("Options")) {

					if (ImGui.menuItem("Enable Campaign Levels", null, enableCampaignLevels)) {
						UserConfig.set(USERCONFIG_NATIVE_GAME_STR, Boolean.toString(enableCampaignLevels.get()));
						reloadList();
					}

					ImGui.menuItem("Unlock All Levels", null, autoUnlockLevels);

					if (ImGui.menuItem("Apply EQ Mod", null, BuiltinModOptions.applyEqMod)) {
						UserConfig.set(BuiltinModOptions.USERCONFIG_EQ_MOD_STR,
								Boolean.toString(BuiltinModOptions.applyEqMod.get()));
					}

					ImGui.endMenu();
				}

				if (ImGui.beginMenu("Advanced")) {

					ImGui.menuItem("Build Targets", null, buildTargets);
					ImGui.setItemTooltip("Uncheck to prevent aurora from touching any cache files");

					ImGui.endMenu();
				}

				ImGui.endMenuBar();
			}

			if (ImGui.beginTable("modmodetable", 1)) {
				ImGui.tableNextColumn();

				ImGui.pushStyleColor(ImGuiCol.ChildBg, new ImVec4(0.12f, 0.12f, 0.32f, 1));
				ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 9);
				ImGui.beginChild("modmodesection", ImGui.getWindowWidth() - 18, 90);

				ImGui.text(" ");
				ImGui.sameLine();
				if (AuroraStub.integrated) {
					ImGui.pushStyleColor(ImGuiCol.Text, new ImVec4(0, 1f, 0, 1f));
					ImGui.text("Aurora is running in integrated mode. All features are enabled!");
					ImGui.popStyleColor();
				} else {
					ImGui.pushStyleColor(ImGuiCol.Text, new ImVec4(1f, 0, 0, 1f));
					ImGui.text("Aurora is running in standalone mode. Some features wil be disabled.");
					ImGui.popStyleColor();
				}

				if (UserConfig.thumperPath() == null) {
					ImGui.textUnformatted("Thumper Directory is not specified, levels will not be built");
				}

				ImGui.text(" ");
				ImGui.sameLine();
				ImGui.pushFont(Font.getFont("levelfont"));
				ImGui.checkbox("MOD MODE ENABLED", isModModeEnabled);

				ImGui.text(" ");
				ImGui.sameLine();
				if (ImGui.button(AuroraStub.integrated ? "Launch Thumper" : "Build Mods")) {
					if (buildTargets.get()) {
						ModBuilder.buildModsAsync(customs, isModModeEnabled.get(), autoUnlockLevels.get());
					}
				}
				ImGui.popFont();

				ImGui.endChild();
				ImGui.popStyleColor();
				ImGui.popStyleVar();

				ImGui.endTable();
			}
			///

			ImGui.text(" ");

			//
			// Thumper path and mod search paths
			//
			if (ImGui.button("Thumper Game Install Location")) {
				UserConfig.properties.remove("thumper.path");
				UserConfig.thumperPath();
			}

			ImGui.sameLine();

			ImGui.textUnformatted(UserConfig.thumperPath());

			ImGui.separatorText("Mod Search Paths");
			GuiPreferences.modSearchPathPanel();

			ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, new ImVec2(0, 10));
			ImGui.text(" ");
			ImGui.popStyleVar();
			/// Mod list and search

			ImGui.separatorText("Custom Level List");
			ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, new ImVec2(-257f, 5f));
			ImGui.pushStyleVar(ImGuiStyleVar.ItemInnerSpacing, new ImVec2(20f, 5f));
			filter.draw("Filter level names (prefix '-' to exclude)", ImGui.getWindowWidth() / 3);
			ImGui.popStyleVar();

			ImGui.sameLine();
			ImGui.pushStyleColor(ImGuiCol.Button, new ImVec4(0.4f, 0, 0, 1));
			ImGui.pushStyleColor(ImGuiCol.ButtonHovered, new ImVec4(1f, 0, 0, 1));
			if (ImGui.button("X")) {
				filter.setInputBuffer("");
				filter.build();
			}
			ImGui.popStyleColor(2);
			ImGui.popStyleVar();

			var buttonrefresh = TextureRegistry.get("button_icons/icon-refresh.png");
			ImGui.pushStyleColor(ImGuiCol.Button, new ImVec4(0.2f, 0.2f, 0.2f, 1));
			ImGui.pushStyleColor(ImGuiCol.ButtonHovered, new ImVec4(0.7f, 0.7f, 0.7f, 1));
			if (ImGui.imageButton("Reload", buttonrefresh.getHandle(), 18, 18)) reloadList();
			ImGui.sameLine();
			ImGui.popStyleColor(2);

			ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, new ImVec2(3f, 3f));
			ImGui.pushStyleColor(ImGuiCol.Button, new ImVec4(0, 0.4f, 0, 1));
			ImGui.pushStyleColor(ImGuiCol.ButtonHovered, new ImVec4(0, 1f, 0, 1));
			if (ImGui.button("Select All")) {
				for (var custom : customs) {
					custom.enabled.set(true);
					UserConfig.setModEnabled(custom.tcl.levelName, true);
				}
			}
			ImGui.popStyleColor(2);

			ImGui.sameLine();
			ImGui.pushStyleColor(ImGuiCol.Button, new ImVec4(0.4f, 0, 0, 1));
			ImGui.pushStyleColor(ImGuiCol.ButtonHovered, new ImVec4(1f, 0, 0, 1));
			if (ImGui.button("Deselect All")) {
				for (var custom : customs) {
					custom.enabled.set(false);
					UserConfig.setModEnabled(custom.tcl.levelName, false);
				}
			}
			ImGui.popStyleColor(2);

			ImGui.sameLine();
			if (ImGui.checkbox("Show Selected Only", showselected)) {
				showselected = !showselected;
			}
			ImGui.popStyleVar();

			ImGui.sameLine();
			ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, new ImVec2(3f, 5f));
			ImGui.text("         Sort:");
			ImGui.sameLine();
			ImGui.pushStyleColor(ImGuiCol.Button, new ImVec4(0.4f, 0, 0.4f, 1));
			ImGui.pushStyleColor(ImGuiCol.ButtonHovered, new ImVec4(1f, 0, 1f, 1));
			if (ImGui.button("Name")) {
				namesortorder = !namesortorder;

				customs.sort((obj1, obj2) -> {
					if (namesortorder)
						return obj1.tcl.levelName.toUpperCase().compareTo(obj2.tcl.levelName.toUpperCase());
					return obj2.tcl.levelName.toUpperCase().compareTo(obj1.tcl.levelName.toUpperCase());
				});
			}
			ImGui.sameLine();
			if (ImGui.button("Difficulty")) {
				ranksortorder = !ranksortorder;

				customs.sort((obj1, obj2) -> {
					if (ranksortorder) return obj1.tcl.difficulty.compareTo(obj2.tcl.difficulty);
					return obj2.tcl.difficulty.compareTo(obj1.tcl.difficulty);
				});
			}
			ImGui.popStyleColor(2);
			ImGui.popStyleVar();

			ImGui.separator();

			if (ImGui.beginChild("modview")) {
				if (UserConfig.modPaths.isEmpty()) {
					ImGui.textUnformatted("No mod search paths are present. Add one above!");
				}

				if (ImGui.beginTable("modtable", 2, ImGuiTableFlags.Resizable | ImGuiTableFlags.BordersInnerV)) {

					ImGui.tableNextColumn();

					if (ImGui.beginChild("Mod Listing")) {
						int id = 0;
						for (var custom : customs) {
							if (!filter.passFilter(custom.tcl.levelName) && !filter.passFilter(custom.tcl.author)
									&& !filter.passFilter(custom.tcl.author.replace(" ", ""))
									&& !filter.passFilter(custom.tcl.author.replace("-", "")))
								continue;
							if (showselected && !custom.enabled.get()) continue;

							ImGui.pushID(id++);

							if (ImGui.checkbox("##active", custom.enabled)) {
								UserConfig.setModEnabled(custom.tcl.levelName, custom.enabled.get());
							}

							ImGui.sameLine();
							ImGui.selectable(custom.tcl.levelName, custom == selected);
							ImGui.setItemTooltip(custom.origin);

							if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(0)) {
								custom.enabled.set(!custom.enabled.get());
								UserConfig.setModEnabled(custom.tcl.levelName, custom.enabled.get());
							}
							if (ImGui.isItemClicked()) {
								selected = custom;
							}

							if (custom.tcl.difficulty != null && !custom.tcl.difficulty.isEmpty()) {

								String match = "scaled_" + custom.tcl.difficulty.toLowerCase() + ".png";

								var texture = TextureRegistry.get("difficulty_icons/" + match);

								if (texture != null) {
									// ImGui.sameLine();
									float size = ImGui.getFrameHeight();
									float width = size * texture.getAspect();
									float offset = ImGui.getContentRegionAvailX() - width
											- ImGui.getStyle().getItemSpacingX() - ImGui.getStyle().getFramePaddingX();

									ImGui.sameLine(ImGui.getCursorPosX() + offset, ImGui.getStyle().getItemSpacingX());

									ImGui.image(texture.getHandle(), width, size);

								}
							}

							ImGui.popID();
						}
					}
					ImGui.endChild();

					ImGui.tableNextColumn();

					ImGui.pushStyleColor(ImGuiCol.ChildBg, new ImVec4(0.12f, 0.12f, 0.12f, 1));
					if (ImGui.beginChild("Mod Properties")) {
						if (selected != null) {
							ImGui.pushFont(Font.getFont("levelfont"));
							ImGui.text("  " + selected.tcl.levelName);
							ImGui.popFont();
							if (selected.tcl.difficulty != null && !selected.tcl.difficulty.isEmpty()) {
								String match = selected.tcl.difficulty.toLowerCase() + "_large.png";
								var texture = TextureRegistry.get("difficulty_icons/" + match);
								if (texture != null) {
									// float size = ImGui.getFrameHeight();
									ImGui.sameLine(ImGui.getCursorPosX(), ImGui.getStyle().getItemSpacingX());
									ImGui.image(texture.getHandle(), 50, 50);
								}
							}
							railColorsTop();

							ImGui.textUnformatted("  Author:");
							String[] authors = selected.tcl.author.replace(" ", "").split(",");
							for (var author : authors) {
								ImGui.sameLine();
								if (ImGui.button(author)) {
									filter.setInputBuffer(author);
									filter.build();
								}
							}

							ImGui.text("  Difficulty: " + selected.tcl.difficulty);
							ImGui.sameLine();

							ImGui.text("||  " + selected.tcl.bpm + " BPM");
							ImGui.sameLine();

							ImGui.text("||  " + selected.tcl.sections.size() + " Sublevels");

							ImGui.text("  Description:");

							ImGui.text(" ");
							ImGui.sameLine();
							ImGui.pushStyleColor(ImGuiCol.ChildBg, new ImVec4(0.12f, 0.12f, 0.32f, 1));
							ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 9);
							ImGui.beginChild("leveldesc", ImGui.getColumnWidth() - 30, 180);
							ImGui.text(" ");
							ImGui.sameLine();
							ImGui.textWrapped(selected.tcl.description);
							ImGui.endChild();
							ImGui.popStyleVar();
							ImGui.popStyleColor();

							railColorsBottom();

							drawSpeedMod();
						}
					}
					ImGui.endChild();
					ImGui.popStyleColor();

					ImGui.endTable();
				}
			}
			ImGui.endChild();
		}
		ImGui.popStyleVar();
		ImGui.end();
	}

	private static void railColorsTop() {
		ImGui.pushStyleColor(ImGuiCol.Separator, selected.tcl.railsGlowColor.toImVec4wxyz(null));
		ImGui.separator();
		ImGui.pushStyleColor(ImGuiCol.Separator, selected.tcl.railsColor.toImVec4wxyz(null));
		ImGui.separator();
		ImGui.pushStyleColor(ImGuiCol.Separator, selected.tcl.pathColor.toImVec4wxyz(null));
		ImGui.separator();
		ImGui.popStyleColor(3);
	}

	private static void railColorsBottom() {
		ImGui.pushStyleColor(ImGuiCol.Separator, selected.tcl.pathColor.toImVec4wxyz(null));
		ImGui.separator();
		ImGui.pushStyleColor(ImGuiCol.Separator, selected.tcl.railsColor.toImVec4wxyz(null));
		ImGui.separator();
		ImGui.pushStyleColor(ImGuiCol.Separator, selected.tcl.railsGlowColor.toImVec4wxyz(null));
		ImGui.separator();
		ImGui.popStyleColor(3);
	}

	private static void drawSpeedMod() {
		ImGui.pushStyleVar(ImGuiStyleVar.GrabMinSize, 20);
		ImGui.pushStyleVar(ImGuiStyleVar.GrabRounding, 5);
		ImGui.sliderInt("Speed Modifier", selected.speedModifier, 10, 300, "%d%%");
		ImGui.popStyleVar(2);

		ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 3f, 3f);
		ImGui.pushStyleColor(ImGuiCol.Button, 0f, 0.4f, 0f, 1f);
		ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0f, 1f, 0f, 1f);
		for (var speed : new int[] { 50, 75, 90, 100, 110, 125, 150 }) {
			if (ImGui.button(Float.toString(speed / 100.0f) + "x", 48f, 25f)) selected.speedModifier[0] = speed;
			ImGui.sameLine();
		}
		ImGui.popStyleColor(2);
		ImGui.popStyleVar();
		ImGui.dummy(0, 0);
	}

}
