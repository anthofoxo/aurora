#pragma once

#include <filesystem>
#include <optional>
#include <span>
#include <string_view>
#include <string>
#include <vector>

namespace aurora {
    std::uint32_t hash(std::span<std::byte const> bytes);

    std::string unescape(std::string_view input);
    std::string escape(std::string_view input);

    std::optional<std::vector<std::byte>> read_file(std::filesystem::path const& aPath);
    bool write_file(std::filesystem::path const& aPath, std::span<std::byte const> aBytes);
}
