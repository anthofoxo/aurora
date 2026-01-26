package xyz.anthofoxo.aurora;

import static java.nio.file.StandardWatchEventKinds.*;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.libc.LibCStdlib;

import xyz.anthofoxo.aurora.audio.AudioBuffer;
import xyz.anthofoxo.aurora.audio.AudioSource;
import xyz.anthofoxo.aurora.target.Target;
import xyz.anthofoxo.aurora.target.Tcle3;
import xyz.anthofoxo.aurora.tml.TMLBuilder;

public class Tcle3Watcher {

	private static WatchService watcher;
	private static Map<WatchKey, String> keys = new HashMap<>();

	static {
		try {
			watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static AudioBuffer buffer;
	public static AudioSource source;

	public static void setup(List<Target> targets) throws IOException {
		for (var target : targets) {
			if (target instanceof Tcle3) {
				if (!target.enabled.get()) continue;
				var path = Path.of(target.origin);
				keys.put(path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY), target.origin);
			}
		}

		source = new AudioSource();
		buffer = new AudioBuffer();

		try (MemoryStack stack = stackPush()) {
			IntBuffer channels = stack.mallocInt(1);
			IntBuffer sampleRate = stack.mallocInt(1);

			byte[] file = Util.getResourceBytes("reload.ogg");
			ByteBuffer oggbuffer = MemoryUtil.memAlloc(file.length);
			oggbuffer.put(0, file);
			ShortBuffer pcm = stb_vorbis_decode_memory(oggbuffer, channels, sampleRate);
			MemoryUtil.memFree(oggbuffer);

			try {
				int format = channels.get(0) == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;
				buffer.data(format, pcm, sampleRate.get(0));
			} finally {
				LibCStdlib.free(pcm);
			}
		}

		source.setBuffer(buffer);
	}

	public static void update() {
		boolean rebuiltAny = false;

		for (var entry : keys.entrySet()) {
			var key = entry.getKey();

			boolean update = false;

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();
				if (kind == OVERFLOW) continue;

				WatchEvent<Path> ev = (WatchEvent<Path>) event;
				Path filename = ev.context();
				if (!filename.toString().toLowerCase().endsWith(".tcl")) continue;

				update = true;
				// dont early break, make sure everything is polled
			}

			if (!update) {
				key.reset();
				continue;
			}

			System.out.println("Detected change: " + entry.getValue());

			try {
				var modTarget = new Tcle3(Path.of(entry.getValue()));
				var asset = modTarget.build(1.0f);
				var thumperdir = UserConfig.thumperPath();

				// write objlib
				int target = Hash.fnv1a(String.format("Alevels/custom/%s.objlib", asset.levelName));
				TMLBuilder.writefileBackedup(thumperdir + "/cache/" + Integer.toHexString(target) + ".pc",
						asset.objlib);

				// write sec
				target = Hash.fnv1a(String.format("Alevels/custom/%s.sec", asset.levelName));
				TMLBuilder.writefileBackedup(thumperdir + "/cache/" + Integer.toHexString(target) + ".pc", asset.sec);

				// write pc files
				for (var pc : asset.pcFiles.entrySet()) {
					TMLBuilder.writefileBackedup(thumperdir + "/cache/" + pc.getKey(), pc.getValue());
				}

				rebuiltAny = true;
			} catch (IOException e) {
				e.printStackTrace();
			}

			key.reset();
		}

		if (rebuiltAny) {
			source.play();
		}
	}

}
