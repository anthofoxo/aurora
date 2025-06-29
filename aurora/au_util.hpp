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
    std::string unescape(std::string_view input);
    std::string escape(std::string_view input);

    [[nodiscard]] std::optional<std::vector<std::byte>> read_file(std::filesystem::path const& aPath);
    bool write_file(std::filesystem::path const& aPath, std::span<std::byte const> aBytes);

    template<typename T>
    [[nodiscard]] constexpr bool is_future_ready(std::future<T> const& future) noexcept {
        return future.wait_until(std::chrono::steady_clock::time_point::min()) == std::future_status::ready;
    }

    std::string const& get_program_files_directory();

    template <auto fn>
    struct DeleterOf {
        template <typename T>
        constexpr void operator()(T* arg) const { fn(arg); }
    };
}
