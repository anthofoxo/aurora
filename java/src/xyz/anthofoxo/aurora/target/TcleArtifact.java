package xyz.anthofoxo.aurora.target;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import xyz.anthofoxo.aurora.struct.AuroraReader;
import xyz.anthofoxo.aurora.struct.AuroraWriter;
import xyz.anthofoxo.aurora.struct.Sample;
import xyz.anthofoxo.aurora.tml.TCLFile;

public class TcleArtifact extends Target {

	private ZipFile zipFile;
	private List<ZipEntry> paths = new ArrayList<>();

	public TcleArtifact(Path path) throws ZipException, IOException {
		super(path);

		zipFile = new ZipFile(path.toFile());
		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		boolean hasobjlib = false;

		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.isDirectory()) continue;

			if (".objlib".equals(getExtension(entry.getName()))) hasobjlib = true;

			else if (".tcl".equals(getExtension(entry.getName()))) {
				try (var stream = zipFile.getInputStream(entry)) {
					tcl = TCLFile.parse(JSON_MAPPER.readTree(stream.readAllBytes()));
				}
			}

			paths.add(entry);
		}

		if (tcl == null) throw new IllegalStateException("No TCL file found");
		if (!hasobjlib) throw new IllegalStateException("No Objlib found");
	}

	@Override
	public CompiledTarget build(float speedModifier) throws IOException {
		CompiledTarget compiled = new CompiledTarget();

		compiled.levelName = tcl.levelName;
		compiled.localizationKey = String.format("custom.%s", tcl.levelName);
		compiled.localizationValue = tcl.levelName;

		compiled.pcFiles = new HashMap<String, byte[]>();

		for (var entry : paths) {
			try (var stream = zipFile.getInputStream(entry)) {
				String filename = entry.getName();
				var bytes = stream.readAllBytes();

				if (filename.endsWith(".objlib")) {

					compiled.objlib = bytes;

					if (speedModifier != 1.0f) {

						int pos = 0;
						byte[] workingBytes = compiled.objlib.clone();

						while (true) {

							AuroraReader reader = new AuroraReader(workingBytes);
							reader.pos = pos;

							if (reader.seekToi8((byte) 12, (byte) 0, (byte) 0, (byte) 0, (byte) 4, (byte) 0, (byte) 0,
									(byte) 0, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0x12, (byte) 0xFB,
									(byte) 0x8E, (byte) 0x3C) == -1)
								break;

							int findPos = reader.pos;
							Sample sample = reader.obj(Sample.class);
							int sampleEnd = reader.pos;

							sample.pitch *= speedModifier;
							sample.channelGroup = "sequin.ch";

							byte[] before = Arrays.copyOfRange(workingBytes, 0, findPos);
							byte[] after = Arrays.copyOfRange(workingBytes, sampleEnd, workingBytes.length);

							AuroraWriter writer = new AuroraWriter();
							writer.i8arr(before);
							writer.obj(sample);
							writer.i8arr(after);

							workingBytes = writer.getBytes();

							int newSampleSize = writer.position() - before.length - after.length;
							if (newSampleSize <= 0) {
								throw new IllegalStateException("Invalid sample write");
							}

							pos = findPos + newSampleSize;
						}

						compiled.objlib = workingBytes;
					}

					int floatBits = 0;

					floatBits |= (compiled.objlib[compiled.objlib.length - 0x5F - 4] & 0xFF) << 0;
					floatBits |= (compiled.objlib[compiled.objlib.length - 0x5F - 3] & 0xFF) << 8;
					floatBits |= (compiled.objlib[compiled.objlib.length - 0x5F - 2] & 0xFF) << 16;
					floatBits |= (compiled.objlib[compiled.objlib.length - 0x5F - 1] & 0xFF) << 24;

					float bpm = Float.intBitsToFloat(floatBits);
					bpm *= speedModifier;
					floatBits = Float.floatToRawIntBits(bpm);

					compiled.objlib[compiled.objlib.length - 0x5F - 4] = (byte) (floatBits >> 0);
					compiled.objlib[compiled.objlib.length - 0x5F - 3] = (byte) (floatBits >> 8);
					compiled.objlib[compiled.objlib.length - 0x5F - 2] = (byte) (floatBits >> 16);
					compiled.objlib[compiled.objlib.length - 0x5F - 1] = (byte) (floatBits >> 24);

				} else if (filename.endsWith(".sec")) {
					compiled.sec = bytes;
				} else if (filename.endsWith(".pc")) {
					compiled.pcFiles.put(filename, bytes);
				}
			}
		}

		return compiled;
	}

}
