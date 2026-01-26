package xyz.anthofoxo.aurora.audio;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.EXTThreadLocalContext.alcSetThreadContext;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.system.MemoryUtil;

public class AudioEngine implements AutoCloseable {
	public long device;
	public ALCCapabilities deviceCaps;

	public long context;
	public ALCapabilities contextCaps;

	public boolean useTLC;

	public static void checkALCError(long device) {
		int err = alcGetError(device);
		if (err != ALC_NO_ERROR) {
			throw new RuntimeException(alcGetString(device, err));
		}
	}

	public AudioEngine() {
		device = alcOpenDevice((ByteBuffer) null);

		if (device == NULL) {
			throw new IllegalStateException("Failed to open an OpenAL device.");
		}

		deviceCaps = ALC.createCapabilities(device);

		if (!deviceCaps.OpenALC10) {
			throw new IllegalStateException();
		}

		context = alcCreateContext(device, (IntBuffer) null);
		checkALCError(device);

		useTLC = deviceCaps.ALC_EXT_thread_local_context && alcSetThreadContext(context);
		if (!useTLC) {
			if (!alcMakeContextCurrent(context)) {
				throw new IllegalStateException();
			}
		}
		checkALCError(device);

		contextCaps = AL.createCapabilities(deviceCaps, MemoryUtil::memCallocPointer);
	}

	@Override
	public void close() {
		alcMakeContextCurrent(NULL);

		if (useTLC) {
			AL.setCurrentThread(null);
		} else {
			AL.setCurrentProcess(null);
		}

		memFree(contextCaps.getAddressBuffer());

		alcDestroyContext(context);
		alcCloseDevice(device);
	}
}
