package xyz.anthofoxo.aurora.audio;

import static org.lwjgl.openal.AL10.*;

public final class AudioUtil {
	private AudioUtil() {
	}

	public static void checkALError() {
		int err = alGetError();
		if (err != AL_NO_ERROR) {
			throw new RuntimeException(alGetString(err));
		}
	}
}
