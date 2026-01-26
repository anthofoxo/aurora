package xyz.anthofoxo.aurora;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.lwjgl.glfw.GLFW;

import imgui.ImGui;

public class AudioExtract {
	private static int okCount = 0;
	private static List<String> problems = Collections.synchronizedList(new ArrayList<String>());
	private static CompletableFuture<Void> future;
	private static boolean wantOpen = false;

	public static void open() {
		wantOpen = true;

	}

	public static void draw() {

		if (wantOpen) {
			ImGui.openPopup("audioExtract");
			wantOpen = false;
		}

		boolean shouldBegin = false;

		if (ImGui.beginPopupModal("audioExtract")) {
			if (future == null) {

				if (UserConfig.get("aurora.vgmstream", null) == null) {
					ImGui.textColored(255, 0, 0, 255, "Error: vgmstream is not found");

					if (ImGui.button("Download VGMStream")) {
						Util.openURL(
								"https://github.com/vgmstream/vgmstream-releases/releases/download/nightly/vgmstream-win64.zip");
					}

					ImGui.setItemTooltip("Download vgmstream and extract it somewhere");

					ImGui.sameLine();
				}

				if (ImGui.button("Find VGMStream")) {
					var vgmstream = UserConfig.pickVgmstreamPath();
					if (vgmstream != null) {
						UserConfig.set("aurora.vgmstream", vgmstream);
						UserConfig.save();
					}
				}

				ImGui.beginDisabled(UserConfig.get("aurora.vgmstream", null) == null);
				if (ImGui.button("Begin")) shouldBegin = true;
				ImGui.endDisabled();

				ImGui.sameLine();
				if (ImGui.button("Close")) ImGui.closeCurrentPopup();
			} else {
				if (!future.isDone()) {
					ImGui.progressBar((float) GLFW.glfwGetTime() * -1.0f);
				}
				ImGui.textUnformatted("Extracted " + okCount + " samples");

				if (future.isDone()) {
					if (ImGui.button("Close")) {
						ImGui.closeCurrentPopup();
						future = null;
					}
				}

				ImGui.separatorText("Problems");

				for (var problem : problems) {
					ImGui.textUnformatted(problem);
				}
			}

			ImGui.endPopup();
		}

		if (shouldBegin) {
			future = CompletableFuture.runAsync(() -> {
				performExtract();
			});
		}
	}

	private static void performExtract() {
		okCount = 0;
		problems.clear();

		var vgmstream = UserConfig.get("aurora.vgmstream", null);
		if (vgmstream == null) {
			vgmstream = UserConfig.pickVgmstreamPath();
			if (vgmstream != null) {
				UserConfig.set("aurora.vgmstream", vgmstream);
				UserConfig.save();
			}
		}

		if (vgmstream == null) {
			System.out.println("vgmstream not found, operation cancelled");
			return;
		}

		System.out.println("vgmstream is detected");
		System.out.println("Decompressing may take a couple minutes");

		Path extractPath = Path.of("generated/thumper_samples");

		try {
			Files.createDirectories(extractPath);
		} catch (IOException e) {
			System.err.println("Failed to create output dir, cancelled");
			e.printStackTrace();
			return;
		}

		for (var hash : Hash.hashes.entrySet()) {
			// Only accept .wav hash sources
			if (!new String(hash.getValue()).endsWith(".wav")) continue;

			try {
				// Copy from source to a temp file so we can invoke vgmstream
				byte[] data = Files.readAllBytes(
						Path.of(UserConfig.thumperPath(), "cache", Integer.toHexString(hash.getKey()) + ".pc"));
				Files.write(Path.of("generated/thumper_samples", "temp.fsb"), Arrays.copyOfRange(data, 4, data.length));

				String targetFile = "generated/thumper_samples/" + new String(hash.getValue());
				Files.createDirectories(Path.of(targetFile).getParent());

				var process = new ProcessBuilder(vgmstream,
						Path.of("generated/thumper_samples/temp.fsb").toAbsolutePath().toString(), "-o",
						Path.of(targetFile).toAbsolutePath().toString())
						.directory(new File(new File(vgmstream).getParent())).start();

				try {
					int exit = process.waitFor();

					if (exit != 0) {
						System.out.println("Error " + exit);
						problems.add("Error: " + new String(hash.getValue()) + " returned " + exit);
					} else {
						++okCount;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (NoSuchFileException e) {
				var ex = new RuntimeException("Cache file not found for: " + new String(hash.getValue()), e);

				ex.printStackTrace();
				problems.add(ex.toString());
			} catch (IOException e) {
				e.printStackTrace();
				problems.add(e.toString());
			}
		}

		System.out.println("Extracted " + okCount + " files");
	}
}
