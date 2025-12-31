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

	private AuroraStub() {
	}
}
