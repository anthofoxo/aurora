#include "au_hasher.hpp"

#include "au_hash.hpp"
#include "au_util.hpp"

#include <imgui.h>
#include <misc/cpp/imgui_stdlib.h>

#include <Windows.h>

#include <format>
#include <filesystem>

namespace aurora {
	GuiHasher::GuiHasher() {
		mPathImHex = std::format("{}/ImHex/imhex-gui.exe", get_program_files_directory());
		mPathHxD = std::format("{}/HxD/HxD.exe", get_program_files_directory());
		
		if (!std::filesystem::exists(*mPathImHex)) mPathImHex = std::nullopt;
		if (!std::filesystem::exists(*mPathHxD)) mPathHxD = std::nullopt;
	}

	void GuiHasher::on_gui(bool& aVisible, std::unordered_map<std::uint32_t, std::string> const& aHashtable) {
		if (!aVisible) return;

		ImGui::SetNextWindowSize(ImVec2(640, 480), ImGuiCond_FirstUseEver);
		if (!ImGui::Begin("Hasher", &aVisible)) {
			ImGui::End();
			return;
		}

		if (ImGui::InputText("Input", &mInput)) {
			mResultHash = fnv1a(unescape(mInput));

			std::string const path = std::format("cache/{:x}.pc", mResultHash);
			if (std::filesystem::exists(path)) mResultFile = path;
			else mResultFile = std::nullopt;

			try {
				mHashtableHit = std::nullopt;

				if (mInput.size() <= 8) { // inputs larger than 8 chars will never have a hash hit
					std::uint32_t hashed = std::stoul(mInput, 0, 16);
					if (auto it = aHashtable.find(hashed); it != aHashtable.end()) {
						mHashtableHit = it->second;
					}
				}
			}
			catch (std::invalid_argument const&) {}

			mIsInHashtable = aHashtable.contains(mResultHash);
		}

		ImGui::LabelText("Output", "0x%x", mResultHash);

		if (mResultFile) {
			ImGui::LabelText("File", "%s", mResultFile->c_str());
		}

		if (mHashtableHit) {
			ImGui::LabelText("Hashtable Hit", "%s", mHashtableHit->c_str());
		}

		if (mIsInHashtable) {
			ImGui::TextUnformatted("Found in hashtable");
		}

		ImGui::SeparatorText("Actions");

		if (ImGui::MenuItem("Copy Hash")) {
			char buffer[16];
			snprintf(buffer, sizeof(buffer), "0x%x", mResultHash);
			ImGui::SetClipboardText(buffer);
		}

		if (ImGui::MenuItem("Copy File Path", nullptr, nullptr, mResultFile.has_value())) {
			ImGui::SetClipboardText(mResultFile->c_str());
		}

		if (mPathImHex) {
			if (ImGui::MenuItem("Open File in ImHex", nullptr, nullptr, mResultFile.has_value())) {
				spawn_process_with_path_argument(mPathImHex->c_str(), std::filesystem::absolute(std::filesystem::path(*mResultFile)).generic_string());
			}
		}

		if (mPathHxD) {
			if (ImGui::MenuItem("Open File in HxD", nullptr, nullptr, mResultFile.has_value())) {
				spawn_process_with_path_argument(mPathHxD->c_str(), std::filesystem::absolute(std::filesystem::path(*mResultFile)).generic_string());
			}
		}

		ImGui::End();
	}
}