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

	private List<Target> customs = new ArrayList<>();
	private Target selected = null;
	private ImBoolean buildMods = new ImBoolean(true);
	private ImGuiTextFilter filter = new ImGuiTextFilter();
	private ImBoolean autoUnlockLevels = new ImBoolean(true);

	public ModLauncher() {
		reloadList();
	}

	public void reloadList() {
		customs.clear();
		selected = null;

		try (var stream = Files.list(Path.of("aurora_mods"))) {
			for (Path path : stream.collect(Collectors.toList())) {
				try {
					customs.add(new Tcle3(path));
					continue;
				} catch (Exception e) {
				}

				try {
					customs.add(new TcleArtifact(path));
					continue;
				} catch (Exception e) {
				}

				System.out.println("Failed to add target " + path);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void draw() {
		if (ImGui.begin("Launcher", ImGuiWindowFlags.MenuBar)) {
			if (ImGui.beginMenuBar()) {
				if (ImGui.beginMenu("Level Listing")) {

					ImGui.menuItem("Unlock All Levels", null, autoUnlockLevels);

					ImGui.endMenu();
				}

				ImGui.endMenuBar();
			}

			if (ImGui.button("Select All")) {
				for (var custom : customs) {
					custom.enabled.set(true);
				}
			}
			ImGui.sameLine();
			if (ImGui.button("Deselect All")) {
				for (var custom : customs) {
					custom.enabled.set(false);
				}
			}

			filter.draw();

			ImGui.separator();

			if (ImGui.beginChild("modview", ImGui.getWindowWidth(), ImGui.getWindowHeight() - 160)) {
				if (ImGui.beginTable("modtable", 2)) {

					ImGui.tableNextColumn();

					if (ImGui.beginChild("Mod Listing")) {
						int id = 0;
						for (var custom : customs) {
							if (!filter.passFilter(custom.tcl.levelName)) continue;

							ImGui.pushID(id++);

							ImGui.checkbox("##active", custom.enabled);
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
			ImGui.checkbox("Build Mods", buildMods);
			ImGui.setItemTooltip("Disable this checkbox to disable aurora touching the cache files");

			ImGui.sameLine();

			if (ImGui.button("Launch Thumper")) {

				if (buildMods.get()) {
					boolean restoreBackup = true;

					for (var custom : customs) {
						if (custom.enabled.get()) {
							restoreBackup = false;
							break;
						}
					}

					if (restoreBackup) {
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

				Aurora.shouldLaunchThumper = true;
				Aurora.requestClose = true;
			}

		}
		ImGui.end();
	}
}
