package xyz.anthofoxo.aurora.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

import imgui.ImFont;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiTextFilter;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import xyz.anthofoxo.aurora.Aurora;
import xyz.anthofoxo.aurora.AuroraStub;
import xyz.anthofoxo.aurora.EntryPoint;
import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.UserConfig;
import xyz.anthofoxo.aurora.gfx.Font;
import xyz.anthofoxo.aurora.struct.AuroraReader;
import xyz.anthofoxo.aurora.struct.AuroraWriter;
import xyz.anthofoxo.aurora.struct.LevelListingFile;
import xyz.anthofoxo.aurora.target.BuiltinNativeTarget;
import xyz.anthofoxo.aurora.target.Target;
import xyz.anthofoxo.aurora.target.Tcle3;
import xyz.anthofoxo.aurora.target.TcleArtifact;
import xyz.anthofoxo.aurora.tml.TMLBuilder;

public class ModLauncher {

	private ModLauncher() {

	}

	private static List<Target> customs = new ArrayList<>();
	private static Target selected = null;
	private static ImBoolean buildTargets = new ImBoolean(true);
	private static ImBoolean isModModeEnabled = new ImBoolean(true);
	private static ImGuiTextFilter filter = new ImGuiTextFilter();
	private static ImBoolean autoUnlockLevels = new ImBoolean(true);
	private static ImBoolean enableCampaignLevels = new ImBoolean(false);
	private static Boolean showselected = false;
	private static Boolean ranksortorder = false;
	private static Boolean namesortorder = false;

	static {
		reloadList();
	}

	public static void reloadList() {
		for (var element : customs) {
			if (element instanceof Tcle3 target) {
				if (target.texture != null) target.texture.close();
			}
		}

		customs.clear();
		selected = null;

		for (var searchPath : UserConfig.modPaths) {
			try (var stream = Files.list(Path.of(searchPath))) {
				for (Path path : stream.collect(Collectors.toList())) {
					try {
						var target = new Tcle3(path);
						target.enabled.set(UserConfig.isModEnabled(target.tcl.levelName));
						customs.add(target);
						continue;
					} catch (Exception e) {
					}

					try {
						var target = new TcleArtifact(path);
						target.enabled.set(UserConfig.isModEnabled(target.tcl.levelName));
						customs.add(target);
						continue;
					} catch (Exception e) {
					}

					System.out.println("Failed to add target " + path);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (enableCampaignLevels.get()) {
			for (int i = 0; i < 9; ++i) {
				try {
					BuiltinNativeTarget target = new BuiltinNativeTarget(i);
					target.enabled.set(UserConfig.isModEnabled(target.tcl.levelName));
					customs.add(target);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public static void draw() {
		if (ImGui.begin("Launcher", ImGuiWindowFlags.MenuBar)) {
			if (ImGui.beginMenuBar()) {
				if (ImGui.beginMenu("Options")) {

					ImGui.menuItem("Unlock All Levels", null, autoUnlockLevels);

					ImGui.endMenu();
				}

				if (ImGui.beginMenu("Advanced")) {

					ImGui.menuItem("Build Targets", null, buildTargets);
					ImGui.setItemTooltip("Uncheck to prevent aurora from touching any cache files");

					ImGui.endMenu();
				}

				ImGui.endMenuBar();
			}
			
			//
			// Thumper path and mod search paths
			//
			if (ImGui.button("Thumper Game Location")) {
				UserConfig.properties.remove("thumper.path");
				UserConfig.thumperPath();
			}

			ImGui.sameLine();

			ImGui.textUnformatted(UserConfig.thumperPath());

			ImGui.separatorText("Mod Search Paths");

			if (ImGui.button("Add Search Path")) {
				String path = TinyFileDialogs.tinyfd_selectFolderDialog("Mod Search Path", null);

				if (path != null) {
					UserConfig.modPaths.add(path);
					UserConfig.save();
					ModLauncher.reloadList();
				}

			}
			ImGui.sameLine();
			ImGui.pushFont(Font.getFont("defaultsmall"));
			ImGui.text("Any levels found in the listed paths will appear in the level list below.");
			ImGui.popFont();

			int removeIdx = -1;

			for (int i = 0; i < UserConfig.modPaths.size(); ++i) {
				var buttonremove = Aurora.buttonicons.get("icon-remove.png");
				ImGui.pushID(i);
				ImGui.pushStyleColor(ImGuiCol.Button, new ImVec4(0.4f, 0, 0, 1));
				ImGui.pushStyleColor(ImGuiCol.ButtonHovered, new ImVec4(1f, 0, 0, 1));
				if (ImGui.imageButton("buttonremove", buttonremove.getHandle(), 16, 16)) removeIdx = i;
				ImGui.popStyleColor(2);

				ImGui.sameLine();
				
				var buttonfolder = Aurora.buttonicons.get("icon-openfolder.png");
				if (ImGui.imageButton("buttonfolder", buttonfolder.getHandle(), 16, 16)) {
					try {
						Desktop.getDesktop().open(new File(UserConfig.modPaths.get(i)));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				ImGui.sameLine();
				ImGui.pushFont(Font.getFont("consolas"));
				ImGui.textUnformatted(UserConfig.modPaths.get(i));
				ImGui.popFont();
				ImGui.popID();
			}

			if (removeIdx != -1) {
				UserConfig.modPaths.remove(removeIdx);
				UserConfig.save();
				ModLauncher.reloadList();
			}
			///
			/// 
			
			ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, new ImVec2(0, 30));
			ImGui.text(" ");
			ImGui.popStyleVar();
			///
			/// Mod list and search
			/// 
			ImGui.separatorText("Custom Level List");
			filter.draw("Filter level names (prefix '-' to exclude)", ImGui.getWindowWidth() / 3);

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
			
			ImGui.sameLine();
			ImGui.text("         Sort:");
			ImGui.sameLine();
			if (ImGui.button("Rank")) {
				ranksortorder = !ranksortorder;
				Collections.sort(customs, new Comparator<Target>() {
					  public int compare(Target obj1, Target obj2) {
						  if (ranksortorder)
							  return obj1.tcl.difficulty.compareTo(obj2.tcl.difficulty);
						  else
							  return obj2.tcl.difficulty.compareTo(obj1.tcl.difficulty);
					  }
				});
			}
			ImGui.sameLine();
			if (ImGui.button("Name")) {
				namesortorder = !namesortorder;
				Collections.sort(customs, new Comparator<Target>() {
					  public int compare(Target obj1, Target obj2) {
						  if (namesortorder)
							  return obj1.tcl.levelName.toUpperCase().compareTo(obj2.tcl.levelName.toUpperCase());
						  else
							  return obj2.tcl.levelName.toUpperCase().compareTo(obj1.tcl.levelName.toUpperCase());
					  }
				});
			}

			ImGui.separator();

			if (ImGui.beginChild("modview", ImGui.getWindowWidth(), ImGui.getWindowHeight() - 370)) {
				if (UserConfig.modPaths.isEmpty()) {
					ImGui.textUnformatted(
							"No mod search paths are present. Add one above!");
				}
				
				if (ImGui.beginTable("modtable", 2, ImGuiTableFlags.Resizable | ImGuiTableFlags.BordersInnerV )) {

					ImGui.tableNextColumn();

					if (ImGui.beginChild("Mod Listing")) {
						int id = 0;
						for (var custom : customs) {
							if (!filter.passFilter(custom.tcl.levelName)) continue;
							if (showselected && !custom.enabled.get()) continue;

							ImGui.pushID(id++);

							if (ImGui.checkbox("##active", custom.enabled)) {
								UserConfig.setModEnabled(custom.tcl.levelName, custom.enabled.get());
							}

							ImGui.sameLine();
							ImGui.selectable(custom.tcl.levelName, custom == selected);
							if (ImGui.isItemHovered() && ImGui.isMouseDoubleClicked(0)) {
								custom.enabled.set(!custom.enabled.get());
								UserConfig.setModEnabled(custom.tcl.levelName, custom.enabled.get());
							}
							if (ImGui.isItemClicked()) {
								selected = custom;
							}

							if (custom.tcl.difficulty != null && !custom.tcl.difficulty.isEmpty()) {

								String match = custom.tcl.difficulty.toLowerCase() + "_strip.png";

								var texture = Aurora.icons.get(match);

								if (texture != null) {
									// ImGui.sameLine();
									float size = ImGui.getFrameHeight();
									float offset = ImGui.getContentRegionAvailX() - size * 8
											- ImGui.getStyle().getItemSpacingX() - ImGui.getStyle().getFramePaddingX();

									ImGui.sameLine(ImGui.getCursorPosX() + offset, ImGui.getStyle().getItemSpacingX());

									ImGui.image(texture.getHandle(), size * 8, size);

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
							ImGui.text(selected.tcl.levelName);
							ImGui.popFont();
							if (selected.tcl.difficulty != null && !selected.tcl.difficulty.isEmpty()) {
								String match = "alpha_" + selected.tcl.difficulty.toLowerCase() + "_large.png";
								var texture = Aurora.icons.get(match);
								if (texture != null) {
									float size = ImGui.getFrameHeight();
									ImGui.sameLine(ImGui.getCursorPosX(), ImGui.getStyle().getItemSpacingX());
									ImGui.image(texture.getHandle(), 50, 50);
								}
							}
							//separator colors to match the levels rail colors
							ImGui.pushStyleColor(ImGuiCol.Separator, new ImVec4(selected.tcl.railsGlowColor.w, selected.tcl.railsGlowColor.x, selected.tcl.railsGlowColor.y, selected.tcl.railsGlowColor.z));
							ImGui.separator();
							ImGui.popStyleColor();
							ImGui.pushStyleColor(ImGuiCol.Separator, new ImVec4(selected.tcl.railsColor.w, selected.tcl.railsColor.x, selected.tcl.railsColor.y, selected.tcl.railsColor.z));
							ImGui.separator();
							ImGui.popStyleColor();
							ImGui.pushStyleColor(ImGuiCol.Separator, new ImVec4(selected.tcl.pathColor.w, selected.tcl.pathColor.x, selected.tcl.pathColor.y, selected.tcl.pathColor.z));
							ImGui.separator();
							ImGui.popStyleColor();
							
							ImGui.textUnformatted("Author: " + selected.tcl.author);
							ImGui.text("Difficulty: " + selected.tcl.difficulty);

							ImGui.text(selected.tcl.bpm + " BPM");
							ImGui.text(selected.tcl.sections.size() + " Sublevels");
							ImGui.text(" ");
							ImGui.textWrapped("Description:\n" + selected.tcl.description);

							//separator colors to match the levels rail colors
							ImGui.pushStyleColor(ImGuiCol.Separator, new ImVec4(selected.tcl.pathColor.w, selected.tcl.pathColor.x, selected.tcl.pathColor.y, selected.tcl.pathColor.z));
							ImGui.separator();
							ImGui.popStyleColor();
							ImGui.pushStyleColor(ImGuiCol.Separator, new ImVec4(selected.tcl.railsColor.w, selected.tcl.railsColor.x, selected.tcl.railsColor.y, selected.tcl.railsColor.z));
							ImGui.separator();
							ImGui.popStyleColor();
							ImGui.pushStyleColor(ImGuiCol.Separator, new ImVec4(selected.tcl.railsGlowColor.w, selected.tcl.railsGlowColor.x, selected.tcl.railsGlowColor.y, selected.tcl.railsGlowColor.z));
							ImGui.separator();
							ImGui.popStyleColor();
							
							ImGui.sliderInt("Speed Modifier", selected.speedModifier, 10, 300, "%d%%");
							//buttons to quick change speed modifier
							var sizew = 48;
							var sizeh = 25;
							ImGui.pushStyleColor(ImGuiCol.Button, new ImVec4(0, 0.4f, 0, 1));
							ImGui.pushStyleColor(ImGuiCol.ButtonHovered, new ImVec4(0, 1f, 0, 1));
							if (ImGui.button("0.25x", sizew, sizeh)) {
								selected.speedModifier[0] = 25;
							}
							ImGui.popStyleColor(2);
							ImGui.sameLine();
							ImGui.pushStyleColor(ImGuiCol.Button, new ImVec4(0, 0.4f, 0, 1));
							ImGui.pushStyleColor(ImGuiCol.ButtonHovered, new ImVec4(0, 1f, 0, 1));
							if (ImGui.button("0.5x", sizew, sizeh)) {
								selected.speedModifier[0] = 50;
							}
							ImGui.popStyleColor(2);
							ImGui.sameLine();
							ImGui.pushStyleColor(ImGuiCol.Button, new ImVec4(0, 0.4f, 0, 1));
							ImGui.pushStyleColor(ImGuiCol.ButtonHovered, new ImVec4(0, 1f, 0, 1));
							if (ImGui.button("1x", sizew, sizeh)) {
								selected.speedModifier[0] = 100;
							}
							ImGui.popStyleColor(2);
							ImGui.sameLine();
							ImGui.pushStyleColor(ImGuiCol.Button, new ImVec4(0, 0.4f, 0, 1));
							ImGui.pushStyleColor(ImGuiCol.ButtonHovered, new ImVec4(0, 1f, 0, 1));
							if (ImGui.button("1.5x", sizew, sizeh)) {
								selected.speedModifier[0] = 150;
							}
							ImGui.popStyleColor(2);
							ImGui.sameLine();
							ImGui.pushStyleColor(ImGuiCol.Button, new ImVec4(0, 0.4f, 0, 1));
							ImGui.pushStyleColor(ImGuiCol.ButtonHovered, new ImVec4(0, 1f, 0, 1));
							if (ImGui.button("2x", sizew, sizeh)) {
								selected.speedModifier[0] = 200;
							}
							ImGui.popStyleColor(2);
							ImGui.sameLine();
							ImGui.pushStyleColor(ImGuiCol.Button, new ImVec4(0, 0.4f, 0, 1));
							ImGui.pushStyleColor(ImGuiCol.ButtonHovered, new ImVec4(0, 1f, 0, 1));
							if (ImGui.button("3x", sizew, sizeh)) {
								selected.speedModifier[0] = 300;
							}
							ImGui.popStyleColor(2);
							ImGui.sameLine();

						}
					}
					ImGui.endChild();
					ImGui.popStyleColor();

					ImGui.endTable();
				}
			}
			ImGui.endChild();

			ImGui.separator();

			String thumperpath = null;
			UserConfig.thumperPath();

			if (thumperpath == null) {
				ImGui.textUnformatted("Thumper Directory is not specified, levels will not be built");
			}

			if (ImGui.button("Reload")) reloadList();
			ImGui.sameLine();
			ImGui.checkbox("Mod Mode Enabled", isModModeEnabled);

			ImGui.sameLine();

			String text = AuroraStub.integrated ? "Launch Thumper" : "Build Mods";

			if (ImGui.button(text)) {

				if (buildTargets.get()) {
					if (!isModModeEnabled.get()) {
						try {
							TMLBuilder.restoreBackups(Path.of(thumperpath).toString());
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (thumperpath != null) {
						try {
							TMLBuilder.buildLevels(customs, Path.of(thumperpath).toString());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					if (autoUnlockLevels.get()) {
						try {
							String filepath = String.format(thumperpath + "/cache/%s.pc",
									Integer.toHexString(Hash.fnv1a("Aui/thumper.levels")));

							AuroraReader reader = new AuroraReader(Files.readAllBytes(Path.of(filepath)));
							LevelListingFile listing = reader.obj(LevelListingFile.class);

							for (var level : listing.enteries) {
								level.unlocks = "";
								level.defaultLocked = false;
							}

							AuroraWriter out = new AuroraWriter();
							out.obj(listing);
							TMLBuilder.writefileBackedup(filepath, out.getBytes());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				}

				if (AuroraStub.integrated) {
					AuroraStub.shouldLaunchThumper = true;
					EntryPoint.running = false;
				}
			}

			ImGui.sameLine();

			if (AuroraStub.integrated) {
				ImGui.text("Aurora is running in integrated mode. All features are enabled");
			} else {
				ImGui.text("Aurora is running in standalone mode. Some features wil be disabled");
			}

		}
		ImGui.end();
	}
}
