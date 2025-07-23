#include "au_window.hpp"

#include "au_util.hpp"

#include <Windows.h>  // Prevent APIENTRY redefinition

#include <GLFW/glfw3.h>
#include <glad/gl.h>
#include <spdlog/spdlog.h>

#define STB_IMAGE_RESIZE_IMPLEMENTATION
#include "stb_image_resize2.h"

namespace aurora {
namespace {
	std::uint_fast8_t gWindowCount = 0;
}

Window::Window(CreateInfo const& aInfo) {
	if (gWindowCount == 0) {
		glfwSetErrorCallback([](int aErrorCode, char const* aDescription) { spdlog::error("GLFW Error {}: {}", aErrorCode, aDescription); });

		if (!glfwInit()) spdlog::critical("Failed to initialize GLFW");
	}

	auto const* vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

	glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
	glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
	glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
	glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
	glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
	glfwWindowHint(GLFW_POSITION_X, vidmode->width / 2 - aInfo.width / 2);
	glfwWindowHint(GLFW_POSITION_Y, vidmode->height / 2 - aInfo.height / 2);

	mHandle = glfwCreateWindow(aInfo.width, aInfo.height, aInfo.title, nullptr, nullptr);
	if (!mHandle) spdlog::critical("Failed to create GLFW window");

	if (aInfo.iconPixels) {
		std::array<std::unique_ptr<unsigned char, DeleterOf<[](void* p) { STBIR_FREE(p, 0); }>>, 4> imageMemory;
		std::array<GLFWimage, imageMemory.size()> glfwImages;

		for (int i = 0; i < imageMemory.size(); ++i) {
			int const size = (i + 1) * 16;
			imageMemory[i] = decltype(imageMemory)::value_type(stbir_resize_uint8_srgb(aInfo.iconPixels, aInfo.iconWidth, aInfo.iconWidth, 0, nullptr, size, size, 0, STBIR_RGBA));
			glfwImages[i].width = size;
			glfwImages[i].height = size;
			glfwImages[i].pixels = imageMemory[i].get();
		}

		glfwSetWindowIcon(mHandle, static_cast<int>(glfwImages.size()), glfwImages.data());
	}

	glfwMakeContextCurrent(mHandle);
	if (!gladLoadGL(&glfwGetProcAddress)) spdlog::critical("Failed to initialize GLAD");

	++gWindowCount;
}

Window& Window::operator=(Window&& aOther) noexcept {
	std::swap(mHandle, aOther.mHandle);
	return *this;
}

Window::~Window() noexcept {
	if (!mHandle) return;

	glfwMakeContextCurrent(nullptr);
	glfwDestroyWindow(mHandle);
	--gWindowCount;

	if (gWindowCount == 0) {
		glfwTerminate();
	}
}
}  // namespace aurora