package xyz.anthofoxo.aurora;

import static org.lwjgl.glfw.GLFW.glfwSetWindowIcon;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.stb.STBImageResize.STBIR_RGBA;
import static org.lwjgl.stb.STBImageResize.stbir_resize_uint8_srgb;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.libc.LibCStdlib;

import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.app.Application;
import imgui.app.Configuration;
import imgui.flag.ImGuiConfigFlags;

public final class EntryPoint extends Application {
	private Aurora aurora = new Aurora();

	private EntryPoint() {
	}

	private byte[] ttf;

	@Override
	protected void configure(Configuration config) {
		config.setTitle(Aurora.TITLE);
	}

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

	private void setIcons() throws IOException {
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

					glfwSetWindowIcon(handle, images);
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

	@Override
	protected void initWindow(Configuration config) {
		super.initWindow(config);

		try {
			setIcons();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void initImGui(Configuration config) {
		super.initImGui(config);
		ImGui.getIO().addConfigFlags(ImGuiConfigFlags.DockingEnable);

		try (var stream = Util.getResource("NotoSans-Regular.ttf")) {
			ttf = stream.readAllBytes();
			ImFontConfig cfg = new ImFontConfig();
			cfg.setFontDataOwnedByAtlas(false);
			ImGui.getIO().getFonts().addFontFromMemoryTTF(ttf, 18.0f, cfg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process() {
		ImGui.dockSpaceOverViewport();
		aurora.update(this);
	}

	public static boolean auroraMain(boolean integrated) {
		Aurora.integrated = integrated;
		Application.launch(new EntryPoint());
		return Aurora.shouldLaunchThumper;
	}

	public static void main(String[] args) {
		auroraMain(false);
	}
}
