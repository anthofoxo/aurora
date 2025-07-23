#include "au_util.hpp"

#include <fstream>
#include <iomanip>
#include <sstream>

#include <Shlobj.h>
#include <Windows.h>

namespace aurora {
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

std::string unescape(std::string_view const input) {
	std::stringstream output;
	for (size_t i = 0; i < input.length(); ++i) {
		if (input[i] == '\\' && i + 3 < input.length() && input[i + 1] == 'x') {
			std::stringstream hex_value;
			hex_value << input[i + 2] << input[i + 3];  // Exactly 2 hex digits

			unsigned int value;
			hex_value >> std::hex >> value;
			output << static_cast<char>(value);

			i += 3;  // Skip \xHH (4 characters total)
		} else {
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
		} else {
			output << "\\x" << std::hex << std::setw(2) << std::setfill('0') << static_cast<unsigned int>(static_cast<unsigned char>(input[i]));
		}
	}

	return output.str();
}

bool write_file(std::filesystem::path const& aPath, std::span<std::byte const> const aBytes) {
	std::ofstream stream(aPath, std::ios::binary | std::ios::out);
	if (!stream) return false;

	stream.write(reinterpret_cast<char const*>(aBytes.data()), aBytes.size_bytes());
	return true;
}

namespace {
	std::optional<std::string> gProgramFiles;
}

std::string const& get_program_files_directory() {
	if (gProgramFiles) return *gProgramFiles;

	PWSTR path = NULL;
	if (SHGetKnownFolderPath(FOLDERID_ProgramFiles, KF_FLAG_DEFAULT, NULL, &path) == S_OK) {
		int sizeNeeded = WideCharToMultiByte(CP_UTF8, 0, path, -1, nullptr, 0, nullptr, nullptr);
		gProgramFiles = std::string(sizeNeeded - 1, 0);
		WideCharToMultiByte(CP_UTF8, 0, path, -1, gProgramFiles->data(), sizeNeeded, nullptr, nullptr);
	} else {
		gProgramFiles = "C:/Program Files";
	}

	CoTaskMemFree(path);

	return *gProgramFiles;
}
}  // namespace aurora