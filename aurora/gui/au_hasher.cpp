#include "au_hasher.hpp"

#include "au_hash.hpp"
#include "au_util.hpp"

#include <imgui.h>
#include <misc/cpp/imgui_stdlib.h>

#include <Windows.h>

#include <format>
#include <filesystem>

namespace aurora {
	namespace {
		void spawn_process_with_path_argument(std::string const& aApplication, std::string const& aArgumentPath) {
			std::string arguments = std::format("\"{}\" \"{}\"", std::filesystem::path(aApplication).filename().generic_string(), aArgumentPath);

			STARTUPINFOA si;
			ZeroMemory(&si, sizeof(si));
			si.cb = sizeof(si);

			PROCESS_INFORMATION pi;
			ZeroMemory(&pi, sizeof(pi));

			CreateProcessA(aApplication.c_str(), arguments.data(), NULL, NULL, FALSE, 0, NULL, NULL, &si, &pi);

			CloseHandle(pi.hProcess);
			CloseHandle(pi.hThread);
		}
	}

	GuiHasher::GuiHasher() {
		mPathImHex = std::format("{}/ImHex/imhex-gui.exe", get_program_files_directory());
		mPathHxD = std::format("{}/HxD/HxD.exe", get_program_files_directory());
		
		if (!std::filesystem::exists(*mPathImHex)) mPathImHex = std::nullopt;
		if (!std::filesystem::exists(*mPathHxD)) mPathHxD = std::nullopt;
	}

	void GuiHasher::on_gui(bool& aVisible) {
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
		}

		ImGui::LabelText("Output", "0x%x", mResultHash);

		if (mResultFile) {
			ImGui::LabelText("File", "%s", mResultFile->c_str());
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