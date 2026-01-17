package xyz.anthofoxo.aurora;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiPopupFlags;
import imgui.flag.ImGuiWindowFlags;
import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.parse.AuroraWriter;
import xyz.anthofoxo.aurora.struct.LevelListingFile;
import xyz.anthofoxo.aurora.struct.SaveFile;
import xyz.anthofoxo.aurora.struct.SectionFile;
import xyz.anthofoxo.aurora.target.BuiltinNativeTarget;
import xyz.anthofoxo.aurora.target.Target;
import xyz.anthofoxo.aurora.target.Tcle3;
import xyz.anthofoxo.aurora.target.TcleArtifact;
import xyz.anthofoxo.aurora.tml.TMLBuilder;

public class ModBuilder {

	private static final String LISTING_FILE = "/cache/" + Integer.toHexString(Hash.fnv1a("Aui/thumper.levels"))
			+ ".pc";

	private static LevelListingFile getLevelListing() throws IOException {
		AuroraReader reader = new AuroraReader(Files.readAllBytes(Path.of(UserConfig.thumperPath() + LISTING_FILE)));
		return reader.obj(LevelListingFile.class);
	}

	private static void setLevelListing(LevelListingFile file) throws IOException {
		AuroraWriter out = new AuroraWriter();
		out.obj(file);
		TMLBuilder.writefileBackedup(UserConfig.thumperPath() + LISTING_FILE, out.getBytes());
	}

	private static String changeExtension(String filename, String newExt) {
		// Ensure the new extension starts with a dot
		if (!newExt.startsWith(".")) {
			newExt = "." + newExt;
		}

		// Find the last dot in the filename
		int lastDotIndex = filename.lastIndexOf('.');

		// If there's no dot, just append the new extension
		if (lastDotIndex == -1) {
			return filename + newExt;
		}

		// Replace the old extension with the new one
		return filename.substring(0, lastDotIndex) + newExt;
	}

	public static void gui() {
		if (buildProgress != null) {
			ImGui.openPopup("aur_building");
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

				var buildText = ModBuilder.getBuildText();

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
	}

	private static void updateSaveFiles(LevelListingFile listing) throws IOException {

		var now = Instant.now();

		for (var path : Files.walk(Path.of(UserConfig.thumperPath() + "/savedata/")).filter(Files::isRegularFile)
				.collect(Collectors.toList())) {
			if (!path.toString().endsWith(".sav")) continue;

			byte[] bytes = Files.readAllBytes(path);
			AuroraReader in = new AuroraReader(bytes);
			SaveFile file = in.obj(SaveFile.class);

			if (UserConfig.isUnlockPractice()) {
				file.timestamp = now;

				List<SaveFile.LevelEntry> toAdd = new ArrayList<>();

				// ensure the score table has enteries for every level listed
				for (var listingEntry : listing.enteries) {
					int index = file.getLevelSaveIndex(listingEntry.key);

					// This level isn't in our scoring table yet, add a blank one
					if (index != -1) continue;

					System.out.println("No userdata for " + listingEntry.key);

					String filename = Integer.toHexString(Hash.fnv1a("A" + changeExtension(listingEntry.path, "sec")))
							+ ".pc";

					// Read in the section file to know how many sections to add
					byte[] bytes2 = Files.readAllBytes(Path.of(UserConfig.thumperPath() + "/cache/" + filename));
					AuroraReader r = new AuroraReader(bytes2);
					var sectionFile = r.obj(SectionFile.class);
					int numSections = sectionFile.sections.size();

					toAdd.add(SaveFile.LevelEntry.ofDefault(listingEntry.key, numSections));
				}

				file.enteries.addAll(toAdd);

				// Proceed to force practice unlocks
				for (var entry : file.enteries) {
					if ("RANK_NONE".equals(entry.playRank)) {
						entry.playRank = "RANK_C";
					}

					if ("RANK_NONE".equals(entry.playRankDup)) {
						entry.playRankDup = "RANK_C";
					}
				}
			}

			AuroraWriter out = new AuroraWriter();
			out.obj(file);
			Files.write(path, out.getBytes());
		}
	}

	public static void reloadTargetList(List<Target> targets, boolean campeign) {
		for (var element : targets) {
			if (element instanceof Tcle3 target) {
				if (target.texture != null) target.texture.close();
			}
		}

		targets.clear();

		for (var searchPath : UserConfig.modPaths) {
			try (var stream = Files.list(Path.of(searchPath))) {
				for (Path path : stream.collect(Collectors.toList())) {
					List<Throwable> throwables = new ArrayList<Throwable>();

					try {
						var target = new Tcle3(path);
						target.enabled.set(UserConfig.isModEnabled(target.tcl.levelName));
						targets.add(target);
						continue;
					} catch (Exception e) {
						throwables.add(e);
					}

					try {
						var target = new TcleArtifact(path);
						target.enabled.set(UserConfig.isModEnabled(target.tcl.levelName));
						targets.add(target);
						continue;
					} catch (Exception e) {
						throwables.add(e);
					}

					throwables.add(0, new RuntimeException("Failed to add target: " + path.toString()));

					String error = "";

					for (var err : throwables) {
						error += err.toString() + "\n";
					}

					System.err.println(error);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (campeign) {
			for (int i = 0; i < 9; ++i) {
				try {
					BuiltinNativeTarget target = new BuiltinNativeTarget(i);
					target.enabled.set(UserConfig.isModEnabled(target.tcl.levelName));
					targets.add(target);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	private static String buildText = null;

	public static String getBuildText() {
		return buildText;
	}

	private static CompletableFuture<Void> buildProgress;

	public static void buildModsAsync(List<Target> targets, boolean modModeEnabled, boolean unlockLevels) {
		if (buildProgress != null) {
			System.err.println("Building already in progress, ignoring request");
			return;
		}

		buildProgress = CompletableFuture.runAsync(() -> {
			try {
				buildText = null;
				ModBuilder.build(targets, modModeEnabled, unlockLevels);
			} catch (Throwable e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				buildText += "\n" + sw.toString();
			}
		});
	}

	private static void build(List<Target> targets, boolean modModeEnabled, boolean unlockLevels) {
		// If the thumper path is not specified then we cannot build mods
		var thumperPath = UserConfig.thumperPath();
		if (thumperPath == null) return;

		// if mod mode is disabled then simply restore the backup files and proceed as
		// normal
		if (!modModeEnabled) {
			try {
				TMLBuilder.restoreBackups(Path.of(thumperPath).toString());
			} catch (IOException e) {
				e.printStackTrace();
			}

			return;
		}

		// Build the individual targets
		// This will produce objlibs and secs, along with some other pc files for
		// samples
		try {
			TMLBuilder.buildLevels(targets, Path.of(thumperPath).toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			var levelListing = getLevelListing();

			// If levels should be unlocked, then change their states
			if (unlockLevels) {
				for (var level : levelListing.enteries) {
					level.unlocks = "";
					level.defaultLocked = false;
				}
			}

			setLevelListing(levelListing);

			updateSaveFiles(levelListing);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			if (BuiltinModOptions.applyEqMod.get()) {
				EqMod.apply();
			} else {
				EqMod.restore();
			}
		} catch (IOException e) {
			// propagate and escape vm
			throw new RuntimeException(e);
		}

	}

}
