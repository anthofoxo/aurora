package xyz.anthofoxo.aurora.target;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;

import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.UserConfig;
import xyz.anthofoxo.aurora.struct.AuroraReader;
import xyz.anthofoxo.aurora.struct.AuroraWriter;
import xyz.anthofoxo.aurora.struct.Sample;
import xyz.anthofoxo.aurora.tml.TCLFile;

public class BuiltinNativeTarget extends Target {
	private byte[] objlib;
	private byte[] sec;
	private String key;
	public int footerOffset = 0x5F;

	public BuiltinNativeTarget(int levelidx) throws IOException {

		if (levelidx == 0) {
			footerOffset -= 1;
		}

		String[] objlibs = { Integer.toHexString(Hash.fnv1a("Alevels/demo.objlib")),
				Integer.toHexString(Hash.fnv1a("Alevels/level2/level_2a.objlib")),
				Integer.toHexString(Hash.fnv1a("Alevels/level3/level_3a.objlib")),
				Integer.toHexString(Hash.fnv1a("Alevels/level4/level_4a.objlib")),
				Integer.toHexString(Hash.fnv1a("Alevels/level5/level_5a.objlib")),
				Integer.toHexString(Hash.fnv1a("Alevels/level6/level_6.objlib")),
				Integer.toHexString(Hash.fnv1a("Alevels/level7/level_7a.objlib")),
				Integer.toHexString(Hash.fnv1a("Alevels/level8/level_8a.objlib")),
				Integer.toHexString(Hash.fnv1a("Alevels/level9/level_9a.objlib")), };

		String[] secs = { Integer.toHexString(Hash.fnv1a("Alevels/demo.sec")),
				Integer.toHexString(Hash.fnv1a("Alevels/level2/level_2a.sec")),
				Integer.toHexString(Hash.fnv1a("Alevels/level3/level_3a.sec")),
				Integer.toHexString(Hash.fnv1a("Alevels/level4/level_4a.sec")),
				Integer.toHexString(Hash.fnv1a("Alevels/level5/level_5a.sec")),
				Integer.toHexString(Hash.fnv1a("Alevels/level6/level_6.sec")),
				Integer.toHexString(Hash.fnv1a("Alevels/level7/level_7a.sec")),
				Integer.toHexString(Hash.fnv1a("Alevels/level8/level_8a.sec")),
				Integer.toHexString(Hash.fnv1a("Alevels/level9/level_9a.sec")), };

		this.key = String.format("level%d", levelidx + 1);
		this.objlib = Files.readAllBytes(Path.of(UserConfig.thumperPath() + "/cache/" + objlibs[levelidx] + ".pc"));
		this.sec = Files.readAllBytes(Path.of(UserConfig.thumperPath() + "/cache/" + secs[levelidx] + ".pc"));

		assert (this.objlib != null);
		assert (this.sec != null);

		tcl = new TCLFile();
		tcl.author = "Drool";
		tcl.bpm = 100;
		tcl.description = "Builtin Thumper Level";
		tcl.difficulty = "?";
		tcl.levelName = String.format("Level %d", levelidx + 1);
	}

	@Override
	public CompiledTarget build(float speedModifier) throws IOException {
		CompiledTarget compiled = new CompiledTarget();

		compiled.levelName = tcl.levelName;
		compiled.localizationKey = key;
		compiled.localizationValue = tcl.levelName;

		// none
		compiled.pcFiles = new HashMap<String, byte[]>();

		compiled.objlib = objlib.clone();
		compiled.sec = sec;

		if (speedModifier != 1.0f) {

			int pos = 0;
			byte[] workingBytes = compiled.objlib.clone();

			while (true) {

				AuroraReader reader = new AuroraReader(workingBytes);
				reader.pos = pos;

				if (reader.seekToi8((byte) 12, (byte) 0, (byte) 0, (byte) 0, (byte) 4, (byte) 0, (byte) 0, (byte) 0,
						(byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0x12, (byte) 0xFB, (byte) 0x8E,
						(byte) 0x3C) == -1)
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

		int offset = footerOffset;

		floatBits |= (compiled.objlib[compiled.objlib.length - offset - 4] & 0xFF) << 0;
		floatBits |= (compiled.objlib[compiled.objlib.length - offset - 3] & 0xFF) << 8;
		floatBits |= (compiled.objlib[compiled.objlib.length - offset - 2] & 0xFF) << 16;
		floatBits |= (compiled.objlib[compiled.objlib.length - offset - 1] & 0xFF) << 24;

		float bpm = Float.intBitsToFloat(floatBits);
		bpm *= speedModifier;
		floatBits = Float.floatToRawIntBits(bpm);

		compiled.objlib[compiled.objlib.length - offset - 4] = (byte) (floatBits >> 0);
		compiled.objlib[compiled.objlib.length - offset - 3] = (byte) (floatBits >> 8);
		compiled.objlib[compiled.objlib.length - offset - 2] = (byte) (floatBits >> 16);
		compiled.objlib[compiled.objlib.length - offset - 1] = (byte) (floatBits >> 24);

		return compiled;
	}

}
