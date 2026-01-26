package xyz.anthofoxo.aurora.audio;

import static org.lwjgl.openal.AL10.*;

import java.nio.ShortBuffer;

public class AudioBuffer implements AutoCloseable {
	private int handle;

	public AudioBuffer() {
		alGetError();
		handle = alGenBuffers();
		AudioUtil.checkALError();
	}

	@Override
	public void close() {
		alDeleteBuffers(handle);
	}

	public void data(int format, ShortBuffer pcm, int sampleRate) {
		alGetError();
		alBufferData(handle, format, pcm, sampleRate);
		AudioUtil.checkALError();
	}

	public int getHandle() {
		return handle;
	}
}
