#include "au_util.hpp"

#include <sstream>

namespace aurora {
    std::uint32_t hash(std::span<std::byte const> const bytes) {
        std::uint32_t value = 0x811c9dc5;

        for (auto const& byte : bytes) {
            value = (value ^ static_cast<std::uint32_t>(byte)) * 0x1000193;
        }

        value *= 0x2001;
        value = value ^ (value >> 0x7);
        value *= 0x9;
        value = value ^ (value >> 0x11);
        value *= 0x21;

        return value;
    }

    std::string unescape(std::string_view const input) {
        std::stringstream output;
        for (size_t i = 0; i < input.length(); ++i) {
            if (input[i] == '\\' && i + 3 < input.length() && input[i + 1] == 'x') {
                std::stringstream hex_value;
                hex_value << input[i + 2] << input[i + 3]; // Exactly 2 hex digits

                unsigned int value;
                hex_value >> std::hex >> value;
                output << static_cast<char>(value);

                i += 3; // Skip \xHH (4 characters total)
            }
            else {
                output << input[i];
            }
        }
        return output.str();
    }

    std::string escape(std::string_view const input) {
        std::stringstream output;
        for (size_t i = 0; i < input.length(); ++i) {
            if (std::isprint(input[i])) {
                output << input[i];
            }
            else {
                output << "\\x" << std::hex << static_cast<int>(input[i]);
            }
        }

        return output.str();
    }
}