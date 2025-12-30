package xyz.anthofoxo.aurora.gfx;

import static org.lwjgl.opengl.GL46C.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL45C.*;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import xyz.anthofoxo.aurora.Util;

public class Texture implements AutoCloseable {
	private int handle;

	public static Texture makeFromResource(String resource) {
		try {
			byte[] bytes = Util.getResourceBytes(resource);
			var buffer = MemoryUtil.memAlloc(bytes.length);
			try {
				buffer.put(0, bytes);
				return Texture.makeFromPNG(buffer);
			} finally {
				MemoryUtil.memFree(buffer);
			}

		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Takes in some png image data and creates a texture from it. If this operation
	 * fails then null is returned.
	 * 
	 * @param data
	 * @return
	 */
	public static Texture makeFromPNG(ByteBuffer buffer) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			var pWidth = stack.mallocInt(1);
			var pHeight = stack.mallocInt(1);
			var pChannels = stack.mallocInt(1);
			var pixels = stbi_load_from_memory(buffer, pWidth, pHeight, pChannels, 4);
			if (pixels == null) return null;
			try {
				return makeFromBytesRGBA(pWidth.get(0), pHeight.get(0), pixels);
			} finally {
				stbi_image_free(pixels);
			}
		}

	}

	/**
	 * Construct a texture from simply pixel data. Reasonable defaults will be
	 * chosen for the texture. The pixel row alignment must match the current GL
	 * state. Default is 4.
	 * 
	 * @param width
	 * @param height
	 * @param pixels
	 * @return
	 */
	public static Texture makeFromBytesRGBA(int width, int height, ByteBuffer pixels) {
		Objects.requireNonNull(pixels);

		int numLevels = 1 + (int) Math.floor(Math.log(Math.max(width, height)) / Math.log(2));

		Texture texture = new Texture(GL_TEXTURE_2D, numLevels, GL_RGBA8, width, height, GL_LINEAR_MIPMAP_LINEAR,
				GL_LINEAR, GL_CLAMP_TO_EDGE);

		texture.upload(0, 0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
		texture.generateMipmaps();

		return texture;
	}

	public Texture(int target, int levels, int internalFormat, int width, int height, int minFilter, int magFilter,
			int wrap) {
		handle = glCreateTextures(target);
		glTextureStorage2D(handle, levels, internalFormat, width, height);
		glTextureParameteri(handle, GL_TEXTURE_MIN_FILTER, minFilter);
		glTextureParameteri(handle, GL_TEXTURE_MAG_FILTER, magFilter);
		glTextureParameteri(handle, GL_TEXTURE_WRAP_S, wrap);
		glTextureParameteri(handle, GL_TEXTURE_WRAP_T, wrap);

		float maxAnisotropy = glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY);
		glTextureParameterf(handle, GL_TEXTURE_MAX_ANISOTROPY, maxAnisotropy);
	}

	public void upload(int level, int xoffset, int yoffset, int width, int height, int format, int type,
			ByteBuffer pixels) {
		glTextureSubImage2D(handle, level, xoffset, yoffset, width, height, format, type, pixels);
	}

	public void generateMipmaps() {
		glGenerateTextureMipmap(handle);
	}

	public void bind(int unit) {
		glBindTextureUnit(unit, handle);
	}

	public int getHandle() {
		return handle;
	}

	@Override
	public void close() {
		glDeleteTextures(handle);
		handle = 0;
	}

}
