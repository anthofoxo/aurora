#pragma once

#include <cstdint>
#include <span>
#include <string_view>

namespace aurora {
template <typename T, std::enable_if_t<sizeof(T) == 1, int> = 0>
constexpr std::uint32_t fnv1a(std::span<T const> bytes) noexcept {
	std::uint32_t value = 0x811c9dc5;

	for (auto byte : bytes) {
		value = (value ^ static_cast<std::uint32_t>(byte)) * 0x1000193;
	}

	// This code is the only change making this not technically fnv1a
	value *= 0x2001;
	value = value ^ (value >> 0x7);
	value *= 0x9;
	value = value ^ (value >> 0x11);
	value *= 0x21;
	// ---

	return value;
}

enum struct Context { kConsteval };

constexpr std::uint32_t fnv1a(auto const* data, std::size_t size) noexcept { return fnv1a(std::span(data, size)); }
constexpr std::uint32_t fnv1a(std::string_view bytes) noexcept { return fnv1a(std::span(bytes)); }
consteval std::uint32_t fnv1a(Context, std::string_view bytes) noexcept { return fnv1a(bytes); }
}  // namespace aurora