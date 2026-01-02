package xyz.anthofoxo.aurora.tml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.parse.AuroraWriter;
import xyz.anthofoxo.aurora.struct.LevelListingFile;
import xyz.anthofoxo.aurora.struct.LocalizationFile;
import xyz.anthofoxo.aurora.target.Target;
import xyz.anthofoxo.aurora.target.Target.CompiledTarget;

public class TMLBuilder {

	/**
	 * Invokes Files.write on the given path but before doing so ensures a backup of
	 * the file is made
	 * 
	 * @throws IOException
	 */
	public static void writefileBackedup(String path, byte[] bytes) throws IOException {
		makeBackup(path);
		Files.write(Path.of(path), bytes);
	}

	public static void makeBackup(String path) throws IOException {
		if (Files.exists(Path.of(path)) && !Files.exists(Path.of(path + ".bak"))) {
			Files.copy(Path.of(path), Path.of(path + ".bak"));
		}
	}

	public static void buildLevels(List<Target> levels, String thumperdir) throws IOException {
		List<CompiledTarget> assets = new ArrayList<>();
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		var futures = new ArrayList<Future<CompiledTarget>>();

		System.out.println("Preparing target compilation...");

		int totalTargets = 0;

		for (var entry : levels) {
			if (!entry.enabled.get()) continue;

			Future<CompiledTarget> future = executor.submit(() -> {
				try {
					CompiledTarget compiled = entry.build((float) entry.speedModifier[0] / 100.0f);
					if (entry.speedModifier[0] != 100) {
						compiled.localizationKey += ".speed" + entry.speedModifier[0];
						compiled.localizationValue += " (" + entry.speedModifier[0] + "%)";
					}

					return compiled;
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;

			});

			futures.add(future);
			totalTargets++;
		}

		System.out.println("Waiting for targets to complete...");

		float completed = 0;
		for (var future : futures) {
			try {
				var result = future.get();
				if (result != null) assets.add(result);

			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			completed++;
			System.out.format("%d%%\n", (int) (completed / totalTargets * 100.0f));
		}

		executor.shutdown();

		System.out.println("Compilation complete...");
		System.out.println("Building strings...");

		// write out the level files
		for (var asset : assets) {
			// write objlib
			int target = Hash.fnv1a(String.format("Alevels/custom/%s.objlib", asset.levelName));
			writefileBackedup(thumperdir + "/cache/" + Integer.toHexString(target) + ".pc", asset.objlib);

			// write sec
			target = Hash.fnv1a(String.format("Alevels/custom/%s.sec", asset.levelName));
			writefileBackedup(thumperdir + "/cache/" + Integer.toHexString(target) + ".pc", asset.sec);

			// write pc files
			for (var pc : asset.pcFiles.entrySet()) {
				writefileBackedup(thumperdir + "/cache/" + pc.getKey(), pc.getValue());
			}
		}

		// Create level listing
		LevelListingFile listings = new LevelListingFile();
		listings.enteries = new ArrayList<>();

		for (int i = 0; i < assets.size(); ++i) {
			var asset = assets.get(i);
			var entry = new LevelListingFile.Entry();
			entry.key = asset.localizationKey;
			entry.unknown0 = 0;
			entry.path = String.format("levels/custom/%s.objlib", asset.levelName);
			entry.unlocks = "";
			entry.defaultLocked = false;
			entry.unknown1 = false;
			entry.triggersCredits = false;
			entry.colorIndex0 = i;
			entry.colorIndex1 = i + (assets.size() + 1);
			listings.enteries.add(entry);
		}

		{
			var entry = new LevelListingFile.Entry();
			entry.key = "level3";
			entry.unknown0 = 0;
			entry.path = "levels/level3/level_3a.objlib";
			entry.unlocks = "";
			entry.defaultLocked = false;
			entry.unknown1 = false;
			entry.triggersCredits = false;
			entry.colorIndex0 = assets.size();
			entry.colorIndex1 = assets.size() + (assets.size() + 1);
			listings.enteries.add(entry);
		}

		{
			AuroraWriter out = new AuroraWriter();
			out.obj(listings);
			writefileBackedup(
					String.format(thumperdir + "/cache/%s.pc", Integer.toHexString(Hash.fnv1a("Aui/thumper.levels"))),
					out.getBytes());
		}

		// @formatter:off
		List<String> localizations = List.of(
				"Aui/strings.da.loc",
				"Aui/strings.de.loc",
				"Aui/strings.en.loc",
				"Aui/strings.es-la.loc",
				"Aui/strings.fi.loc",
				"Aui/strings.fr-ca.loc",
				"Aui/strings.fr.loc",
				"Aui/strings.it.loc",
				"Aui/strings.ja.loc",
				"Aui/strings.ko.loc",
				"Aui/strings.nl.loc",
				"Aui/strings.no.loc",
				"Aui/strings.pl.loc",
				"Aui/strings.pt-br.loc",
				"Aui/strings.ru.loc",
				"Aui/strings.sv.loc",
				"Aui/strings.tr.loc",
				"Aui/strings.zh-s.loc",
				"Aui/strings.zh-t.loc"
			);
		// @formatter:on

		for (String loc : localizations) {
			String path = String.format(thumperdir + "/cache/%s.pc", Integer.toHexString(Hash.fnv1a(loc)));

			AuroraReader in = new AuroraReader(Files.readAllBytes(Path.of(path)));

			LocalizationFile locs = new LocalizationFile();
			locs.read(in);

			for (var asset : assets) {
				int idx = locs.indexOfKey(Hash.fnv1a(asset.localizationKey));
				var entry = new LocalizationFile.Entry(asset.localizationValue, Hash.fnv1a(asset.localizationKey));
				if (idx == -1) {
					locs.enteries.add(entry);
				} else {
					locs.enteries.set(idx, entry);
				}

			}

			AuroraWriter out = new AuroraWriter();
			locs.write(out);

			writefileBackedup(path, out.getBytes());
		}

		System.out.println("Done...");
	}

	public static void restoreBackups(String thumperdir) throws IOException {
		try (var stream = Files.list(Path.of(thumperdir + "/cache/"))) {
			for (Path path : stream.collect(Collectors.toList())) {
				if (path.getFileName().toString().endsWith(".bak")) continue;

				if (Files.exists(Path.of(path.toString() + ".bak"))) {
					Files.copy(Path.of(path.toString() + ".bak"), path, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}
}
