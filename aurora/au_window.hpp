#pragma once

#include <utility>

extern "C" typedef struct GLFWwindow GLFWwindow;

namespace aurora {
	class Window final {
	public:
		struct CreateInfo final {
			int width = 1280;
			int height = 720;
			char const* title = "Aurora";

			unsigned char* iconPixels = nullptr;
			int iconWidth = 0;
			int iconHeight = 0;
		};

		constexpr Window() noexcept = default;
		Window(CreateInfo const& aInfo);
		Window(Window const&) = delete;
		Window& operator=(Window const&) = delete;
		Window(Window&& aOther) noexcept { *this = std::move(aOther); }
		Window& operator=(Window&& aOther) noexcept;
		~Window() noexcept;

		GLFWwindow* handle() const noexcept { return mHandle; }
	private:
		GLFWwindow* mHandle = nullptr;
	};
}