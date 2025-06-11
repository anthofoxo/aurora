#pragma once

#include <filesystem>
#include <future>
#include <optional>
#include <span>
#include <string_view>
#include <string>
#include <chrono>
#include <vector>

namespace aurora {
    std::uint32_t hash(std::span<std::byte const> bytes);

    std::string unescape(std::string_view input);
    std::string escape(std::string_view input);

    [[nodiscard]] std::optional<std::vector<std::byte>> read_file(std::filesystem::path const& aPath);
    bool write_file(std::filesystem::path const& aPath, std::span<std::byte const> aBytes);

    template<typename T>
    [[nodiscard]] constexpr bool is_future_ready(std::future<T> const& future) noexcept {
        return future.wait_until(std::chrono::steady_clock::time_point::min()) == std::future_status::ready;
    }
}
