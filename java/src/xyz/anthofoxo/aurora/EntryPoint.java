package xyz.anthofoxo.aurora;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.stb.STBImageResize.STBIR_RGBA;
import static org.lwjgl.stb.STBImageResize.stbir_resize_uint8_srgb;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.libc.LibCStdlib;

import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

public final class EntryPoint {
	public static boolean running = true;
	public static long window;
	private static ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
	private static ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

	private static Aurora aurora;

	private EntryPoint() {
	}

	private static byte[] ttf;

	private static ByteBuffer readResourceImagePixels(String resource, IntBuffer pWidth, IntBuffer pHeight)
			throws IOException {
		byte[] fileBytes = Util.getResourceBytes(resource);

		ByteBuffer fileBuffer = MemoryUtil.memAlloc(fileBytes.length);
		try {
			fileBuffer.put(0, fileBytes);

			try (MemoryStack stack = MemoryStack.stackPush()) {
				IntBuffer pChannels = stack.mallocInt(1);
				return stbi_load_from_memory(fileBuffer, pWidth, pHeight, pChannels, 4);
			}

		} finally {
			MemoryUtil.memFree(fileBuffer);
		}
	}

	private static void setIcons() throws IOException {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);
			ByteBuffer iconPixels = readResourceImagePixels("icon.png", pWidth, pHeight);
			Objects.requireNonNull(iconPixels);

			try {
				final int numImages = 5;

				GLFWImage.Buffer images = GLFWImage.calloc(numImages, stack);

				try {

					for (int i = 0; i < numImages; ++i) {
						int size = (i + 1) * 16;
						ByteBuffer pixels = stbir_resize_uint8_srgb(iconPixels, pWidth.get(0), pHeight.get(0), 0, null,
								size, size, 0, STBIR_RGBA);
						images.get(i).set(size, size, pixels);
					}

					glfwSetWindowIcon(window, images);
				} finally {
					for (int i = 0; i < numImages; ++i) {
						LibCStdlib.free(images.get(i).pixels(0));
					}
				}
			} finally {
				stbi_image_free(iconPixels);
			}
		}
	}

	private static void update() {
		imGuiGl3.newFrame();
		imGuiGlfw.newFrame();
		ImGui.newFrame();

		ImGui.dockSpaceOverViewport();
		aurora.update();

		glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

		ImGui.render();
		imGuiGl3.renderDrawData(ImGui.getDrawData());
	}

	public static boolean auroraMain(boolean integrated) {
		AuroraStub.integrated = integrated;

		GLFWErrorCallback.createPrint(System.err).set();

		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
		window = glfwCreateWindow(1600, 900, Aurora.TITLE, MemoryUtil.NULL, MemoryUtil.NULL);

		if (window == MemoryUtil.NULL) {
			throw new RuntimeException("Failed to create the GLFW window");
		}

		try {
			setIcons();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (MemoryStack stack = MemoryStack.stackPush()) {
			var pWidth = stack.mallocInt(1);
			var pHeight = stack.mallocInt(1);

			glfwGetWindowSize(window, pWidth, pHeight);
			var vidmode = Objects.requireNonNull(glfwGetVideoMode(glfwGetPrimaryMonitor()));
			glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
		}

		glfwMakeContextCurrent(window);
		GL.createCapabilities(true);
		glfwSwapInterval(1);
		glfwShowWindow(window);

		glfwSetWindowRefreshCallback(window, (long _) -> {
			update();
		});

		ImGui.createContext();
		imGuiGlfw.init(window, true);
		imGuiGl3.init("#version 460 core");

		ImGui.getIO().addConfigFlags(ImGuiConfigFlags.DockingEnable);

		try (var stream = Util.getResource("NotoSans-Regular.ttf")) {
			ttf = stream.readAllBytes();
			ImFontConfig cfg = new ImFontConfig();
			cfg.setFontDataOwnedByAtlas(false);
			ImGui.getIO().getFonts().addFontFromMemoryTTF(ttf, 18.0f, cfg);
		} catch (IOException e) {
			e.printStackTrace();
		}

		aurora = new Aurora();

		while (running) {

			update();

			if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
				final long backupCurrentContext = glfwGetCurrentContext();
				ImGui.updatePlatformWindows();
				ImGui.renderPlatformWindowsDefault();
				glfwMakeContextCurrent(backupCurrentContext);
			}

			glfwSwapBuffers(window);
			glfwPollEvents();
			if (glfwWindowShouldClose(window)) running = false;
		}

		imGuiGl3.shutdown();
		imGuiGlfw.shutdown();
		ImGui.destroyContext();

		Callbacks.glfwFreeCallbacks(window);
		glfwDestroyWindow(window);
		glfwTerminate();
		Objects.requireNonNull(glfwSetErrorCallback(null)).free();

		return AuroraStub.shouldLaunchThumper;
	}

	public static void main(String[] args) {
		auroraMain(false);
	}
}
