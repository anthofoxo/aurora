#pragma once

#include <filesystem>
#include <future>
#include <optional>
#include <span>
#include <string_view>
#include <string>
#include <chrono>
#include <vector>
#include <fstream>

namespace aurora {
    std::string unescape(std::string_view input);
    std::string escape(std::string_view input);

    void spawn_process_with_path_argument(std::string const& aApplication, std::string const& aArgumentPath);

    template<typename T = std::byte, std::enable_if_t<sizeof(T) == 1, int> = 0>
    [[nodiscard]] std::optional<std::vector<T>> read_file(std::filesystem::path const& aPath) {
        std::ifstream stream(aPath, std::ios::binary);
        if (!stream) return std::nullopt;

        stream.seekg(0, std::ios::end);
        auto size = stream.tellg();
        stream.seekg(0, std::ios::beg);

        std::vector<T> buffer;
        buffer.resize(size);
        stream.read(reinterpret_cast<char*>(buffer.data()), size);

        return buffer;
    }

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
