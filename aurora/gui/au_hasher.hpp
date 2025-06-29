#pragma once

#include "au_hash.hpp"

#include <cstdint>
#include <string>
#include <optional>

namespace aurora {
	struct GuiHasher {
		GuiHasher();

		void on_gui(bool& aVisible);

		std::optional<std::string> mPathImHex;
		std::optional<std::string> mPathHxD;

		std::string mInput;
		std::uint32_t mResultHash = fnv1a("");
		std::optional<std::string> mResultFile;
	};
}