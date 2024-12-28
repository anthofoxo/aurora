#include "te_hash.hpp"

#include <unordered_map>
#include <format>
#include <array>

#include <yaml-cpp/yaml.h>
#include <tinyfiledialogs.h>

namespace aurora {
    namespace {
        std::unordered_map<uint32_t, std::string> gHashtable;
    }

    void reload_hashtable() {
        try {
            gHashtable.clear();

            YAML::Node yaml = YAML::LoadFile("hashtable.yaml");

            for (YAML::Node const& node : yaml["known"]) {
                std::string str = node.as<std::string>();
                gHashtable[hash(str)] = std::move(str);
            }

            for (YAML::const_iterator it = yaml["unknown"].begin(); it != yaml["unknown"].end(); ++it) {
                uint32_t hash = it->first.as<uint32_t>();
                std::string str = it->second.as<std::string>();
                gHashtable[hash] = std::move(str);
            }
        }
        catch (YAML::Exception const& e) {
            tinyfd_messageBox("Error loading hashtable.yaml", e.what(), "ok", "error", 1);
        }
    }

	std::string rev_hash(uint32_t hash) {
        auto it = gHashtable.find(hash);
        if (it == gHashtable.end()) return std::format("{:x}", hash);
        return it->second;
	}
}