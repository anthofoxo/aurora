#pragma once

#include "au_hash.hpp"

#include <cstdint>
#include <string>
#include <optional>
#include <unordered_map>

namespace aurora {
	struct GuiHasher {
		GuiHasher();

		void on_gui(bool& aVisible, std::unordered_map<std::uint32_t, std::string> const& aHashtable);

		std::optional<std::string> mPathImHex;
		std::optional<std::string> mPathHxD;

		std::string mInput;
		std::uint32_t mResultHash = fnv1a("");
		std::optional<std::string> mResultFile;
		std::optional<std::string> mHashtableHit;
		bool mIsInHashtable = false;
	};
}