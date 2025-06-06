#pragma once

#include <lua.hpp>

#include <span>
#include <string_view>
#include <string>

namespace aurora {
    std::uint32_t hash(std::span<std::byte const> bytes);

    std::string unescape(std::string_view input);
    std::string escape(std::string_view input);
}