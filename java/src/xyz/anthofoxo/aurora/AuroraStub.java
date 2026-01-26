package xyz.anthofoxo.aurora;

/**
 * This class simply contains static fields that the shared library stub may
 * read or modify before or after the Aurora application executes.
 */
public final class AuroraStub {
	/**
	 * When Aurora is ran via the shared library stub, this value will be set to
	 * <code>true</code>.
	 */
	public static boolean integrated = false;

	/**
	 * When Aurora exits, the stub will read this value to decide if to proceed with
	 * the Thumper launch. For example, simply closing the Aurora window will not
	 * set this value, but clicking "Launch Thumper" will set this value.
	 */
	public static boolean shouldLaunchThumper = false;

	/**
	 * When Aurora exits, this value is checked for, if this is set the false then
	 * the initial terminal window that is spawned with Aurora will be closed.
	 */
	public static boolean keepTerminalWindow = false;

	private AuroraStub() {
	}

	/**
	 * Main function when using the stand-alone runtime.
	 */
	public static void main(String[] args) {
		init();
		shutdown();
	}

	/**
	 * Invoked by the Aurora stub during stream startup.
	 */
	public static void init() {
		EntryPoint.auroraMain();
	}

	/**
	 * Invoked by the Aurora stub during steam event updates.
	 */
	public static void update() {
		Tcle3Watcher.update();
	}

	/**
	 * Invoked by the Aurora stub during steam shutdown.
	 */
	public static void shutdown() {
		EntryPoint.auroraShutdown();
	}
}
