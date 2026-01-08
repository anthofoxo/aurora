package xyz.anthofoxo.aurora.audio;

import static org.lwjgl.openal.AL10.*;

import java.util.Objects;

public class AudioSource implements AutoCloseable {
	private int handle;

	public AudioSource() {
		alGetError();
		handle = alGenSources();
		AudioUtil.checkALError();
	}

	@Override
	public void close() {
		alDeleteSources(handle);
	}

	public void setGain(float gain) {
		alGetError();
		alSourcef(handle, AL_GAIN, gain);
		AudioUtil.checkALError();
	}

	public void setLooping(boolean looping) {
		alGetError();
		alSourcei(handle, AL_LOOPING, looping ? AL_TRUE : AL_FALSE);
		AudioUtil.checkALError();
	}

	public void play() {
		alGetError();
		alSourcePlay(handle);
		AudioUtil.checkALError();
	}

	public void queueBuffers(AudioBuffer buffer) {
		Objects.requireNonNull(buffer);
		alGetError();
		alSourceQueueBuffers(handle, buffer.getHandle());
		AudioUtil.checkALError();
	}

	public int getHandle() {
		return handle;
	}
}
