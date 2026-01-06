package xyz.anthofoxo.aurora;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Manages an Aurora session lock.
 * 
 * A session lock is a simple mechanism to detect if another instance of Aurora
 * is running. If this is found to be the case we can slightly adjust behavior.
 */
public final class SessionLock {
	private SessionLock() {
	}

	private static FileChannel channel;
	private static FileLock lock;

	public static boolean obtainLock() {
		var path = Path.of(UserConfig.thumperPath(), "aurora.lock");

		if (!Files.exists(path)) {
			try {
				Files.writeString(path, "🥑");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			channel = FileChannel.open(path, StandardOpenOption.WRITE);
			lock = channel.tryLock();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return lock != null;
	}

	public static void freeLock() {
		try {
			if (lock != null) lock.close();
			if (channel != null) channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
