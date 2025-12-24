package xyz.anthofoxo.aurora.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import imgui.ImGui;
import imgui.type.ImBoolean;
import xyz.anthofoxo.aurora.Aurora;
import xyz.anthofoxo.aurora.UserConfig;
import xyz.anthofoxo.aurora.tml.TCLFile;
import xyz.anthofoxo.aurora.tml.TMLBuilder;

public class ModLauncher {
	public static class LevelEntry {
		public TCLFile tcl;
		public Path path;
		public ImBoolean enabled = new ImBoolean(true);
		public int[] speedModifier = new int[] { 10 };
	}

	private List<LevelEntry> customs = new ArrayList<>();
	private LevelEntry selected = null;
	private ImBoolean buildMods = new ImBoolean(true);

	public ModLauncher() {
		reloadList();
	}

	public void reloadList() {
		customs.clear();
		selected = null;

		try (var stream = Files.list(Path.of("aurora_mods"))) {
			for (Path path : stream.collect(Collectors.toList())) {
				if (path.getFileName().toString().endsWith(".zip")) continue; // Ignore zips
				if (Files.isRegularFile(path)) continue; // Singular files aren't supported yet

				boolean hasTcl = false;
				boolean hasObjlib = false;
				boolean hasTcl2 = false;

				TCLFile tcl = null;

				try (var substream = Files.list(path)) {
					for (var file : substream.collect(Collectors.toList())) {
						String fname = file.getFileName().toString().toLowerCase();

						if (fname.endsWith(".tcl")) {
							hasTcl = true;
							tcl = TCLFile.parse(TMLBuilder.TML_MAPPER.readTree(file));
						}
						if (fname.endsWith(".objlib")) hasObjlib = true;
						if (fname.startsWith("config_") && fname.endsWith(".txt")) hasTcl2 = true;
					}
				}

				if (hasTcl2) {
					System.err.println(
							path.getFileName() + " is a TCL2 level, these are not supported, update to TCLE 3.x");
					continue;
				}

				if (hasTcl && hasObjlib) {
					System.err.println(path.getFileName() + " is a TCLE Compiled Level, these are not supported yet");
					continue;
				}

				if (!hasTcl) continue; // Not supported
				if (hasObjlib) continue; // Not supported

				LevelEntry entry = new LevelEntry();
				entry.path = path;
				entry.tcl = tcl;
				customs.add(entry);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void draw() {
		if (ImGui.begin("Launcher")) {
			if (ImGui.button("Reload")) reloadList();

			ImGui.columns(2);

			int id = 0;
			for (var custom : customs) {
				ImGui.pushID(id++);

				ImGui.checkbox("##active", custom.enabled);
				ImGui.sameLine();
				if (ImGui.selectable(custom.tcl.levelName, custom == selected)) {
					selected = custom;
				}

				ImGui.popID();
			}

			ImGui.nextColumn();

			if (selected != null) {

				ImGui.text(selected.tcl.bpm + " BPM");
				ImGui.textUnformatted("Author: " + selected.tcl.author);
				ImGui.textWrapped(selected.tcl.description);

				ImGui.sliderInt("Speed Modifier", selected.speedModifier, 1, 50, "%d0%%");

			}

			ImGui.columns(1);
			ImGui.separator();

			String thumperpath = UserConfig.thumperPath();

			if (thumperpath == null) {
				ImGui.textUnformatted("Thumper Directory is not specified, levels will not be built");
			}

			ImGui.checkbox("Build Mods", buildMods);
			ImGui.setItemTooltip("Disable this checkbox to disable aurora touching the cache files");

			if (ImGui.button("Launch Thumper")) {

				if (buildMods.get()) {
					boolean restoreBackup = true;

					for (var custom : customs) {
						if (custom.enabled.get()) restoreBackup = false;
						break;
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
				}

				Aurora.shouldLaunchThumper = true;
				Aurora.requestClose = true;
			}

		}
		ImGui.end();
	}
}
