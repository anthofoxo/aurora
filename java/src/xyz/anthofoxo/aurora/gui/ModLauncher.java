package xyz.anthofoxo.aurora.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import imgui.ImGui;
import imgui.ImGuiTextFilter;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import xyz.anthofoxo.aurora.Aurora;
import xyz.anthofoxo.aurora.EntryPoint;
import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.UserConfig;
import xyz.anthofoxo.aurora.struct.AuroraReader;
import xyz.anthofoxo.aurora.struct.AuroraWriter;
import xyz.anthofoxo.aurora.struct.LevelListingFile;
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

	static {
		reloadList();
	}

	public static void reloadList() {
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
	}

	public static void draw() {
		if (ImGui.begin("Launcher", ImGuiWindowFlags.MenuBar)) {
			if (ImGui.beginMenuBar()) {
				if (ImGui.beginMenu("Level Listing")) {

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

			if (ImGui.button("Select All")) {
				for (var custom : customs) {
					custom.enabled.set(true);
					UserConfig.setModEnabled(custom.tcl.levelName, true);
				}
			}
			ImGui.sameLine();
			if (ImGui.button("Deselect All")) {
				for (var custom : customs) {
					custom.enabled.set(false);
					UserConfig.setModEnabled(custom.tcl.levelName, false);
				}
			}

			filter.draw();

			ImGui.separator();

			if (ImGui.beginChild("modview", ImGui.getWindowWidth(), ImGui.getWindowHeight() - 160)) {
				if (UserConfig.modPaths.isEmpty()) {
					ImGui.textUnformatted(
							"No mod search paths are present, Goto File->Preferences to add a search path");
				}

				if (ImGui.beginTable("modtable", 2)) {

					ImGui.tableNextColumn();

					if (ImGui.beginChild("Mod Listing")) {
						int id = 0;
						for (var custom : customs) {
							if (!filter.passFilter(custom.tcl.levelName)) continue;

							ImGui.pushID(id++);

							if (ImGui.checkbox("##active", custom.enabled)) {
								UserConfig.setModEnabled(custom.tcl.levelName, custom.enabled.get());
							}

							ImGui.sameLine();
							if (ImGui.selectable(custom.tcl.levelName, custom == selected)) {
								selected = custom;
							}

							ImGui.popID();
						}
					}
					ImGui.endChild();

					ImGui.tableNextColumn();

					if (ImGui.beginChild("Mod Properties")) {
						if (selected != null) {
							ImGui.text(selected.tcl.levelName);
							ImGui.text(selected.tcl.difficulty);
							ImGui.text(selected.tcl.bpm + " BPM");
							ImGui.textUnformatted("Author: " + selected.tcl.author);
							ImGui.textWrapped(selected.tcl.description);

							ImGui.separator();

							ImGui.sliderInt("Speed Modifier", selected.speedModifier, 10, 300, "%d%%");

						}
					}
					ImGui.endChild();

					ImGui.endTable();
				}
			}
			ImGui.endChild();

			ImGui.separator();

			String thumperpath = UserConfig.thumperPath();

			if (thumperpath == null) {
				ImGui.textUnformatted("Thumper Directory is not specified, levels will not be built");
			}

			if (ImGui.button("Reload")) reloadList();
			ImGui.sameLine();
			ImGui.checkbox("Mod Mode Enabled", isModModeEnabled);

			ImGui.sameLine();

			String text = Aurora.integrated ? "Launch Thumper" : "Build Mods";

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

				if (Aurora.integrated) {
					Aurora.shouldLaunchThumper = true;
					EntryPoint.running = false;
				}
			}

			ImGui.sameLine();

			if (Aurora.integrated) {
				ImGui.text("Aurora is running in integrated mode. All features are enabled");
			} else {
				ImGui.text("Aurora is running in standalone mode. Some features wil be disabled");
			}

		}
		ImGui.end();
	}
}
