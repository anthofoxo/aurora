#pragma once

#include <string>
#include <string_view>
#include <cstdint>

namespace aurora {
    constexpr uint32_t hash(unsigned char const* array, unsigned int size) {
        uint32_t h = 0x811c9dc5;

        while (size > 0) {
            size--;
            h = (h ^ *array) * 0x1000193;
            array++;
        }

        h *= 0x2001;
        h = (h ^ (h >> 0x7)) * 0x9;
        h = (h ^ (h >> 0x11)) * 0x21;

        return h;
    }

    constexpr uint32_t hash(std::string_view str) {
        return hash((unsigned char const*)str.data(), static_cast<unsigned int>(str.size()));
    }

	std::string rev_hash(uint32_t hash);
	void reload_hashtable();
}