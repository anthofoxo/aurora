#include "au_util.hpp"

#include <iomanip>
#include <sstream>
#include <fstream>

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
            if (std::isprint(static_cast<unsigned char>(input[i]))) {
                output << input[i];
            }
            else {
                output << "\\x" << std::hex << std::setw(2) << std::setfill('0') << static_cast<unsigned int>(static_cast<unsigned char>(input[i]));
            }
        }

        return output.str();
    }

    std::optional<std::vector<std::byte>> read_file(std::filesystem::path const& aPath) {
        std::ifstream stream(aPath, std::ios::binary | std::ios::in);
        if (!stream) return std::nullopt;

        stream.seekg(0, std::ios::end);
        auto const length = stream.tellg();
        stream.seekg(0, std::ios::beg);
        std::vector<std::byte> bytes(length);
        stream.read(reinterpret_cast<char*>(bytes.data()), length);

        return bytes;
    }

    bool write_file(std::filesystem::path const& aPath, std::span<std::byte const> const aBytes) {
        std::ofstream stream(aPath, std::ios::binary | std::ios::out);
        if (!stream) return false;

        stream.write(reinterpret_cast<char const*>(aBytes.data()), aBytes.size_bytes());
        return true;
    }
}