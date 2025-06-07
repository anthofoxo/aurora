#ifdef _WIN32
#	include <Windows.h>
#	undef max
#	undef min
#endif

#include <glad/gl.h>
#include <GLFW/glfw3.h>
#include <imgui.h>
#include <imgui_internal.h>
#include <misc/cpp/imgui_stdlib.h>
#include <backends/imgui_impl_glfw.h>
#include <backends/imgui_impl_opengl3.h>

#include "imspinner.h"
#include "imgui_memory_editor.h"
#include "sha1.hpp"
#include "lua_api.hpp"

#include <lua.hpp>
#include <spdlog/fmt/fmt.h>
#include <tinyfiledialogs.h>

#include <algorithm>
#include <cctype>
#include <cstdlib>
#include <filesystem>
#include <fstream>
#include <future>
#include <locale>
#include <optional>
#include <span>
#include <sstream>
#include <string>
#include <unordered_set>
#include <vector>

#include "au_util.hpp"

ImFont* gVariableSpace = nullptr;
ImFont* gMonoSpace = nullptr;

std::unordered_map<std::uint32_t, std::string> gHashtable;

// trim from start (in place)
inline void ltrim(std::string& s) {
	s.erase(s.begin(), std::find_if(s.begin(), s.end(), [](unsigned char ch) {
		return !std::isspace(ch);
	}));
}

// trim from end (in place)
inline void rtrim(std::string& s) {
	s.erase(std::find_if(s.rbegin(), s.rend(), [](unsigned char ch) {
		return !std::isspace(ch);
	 }).base(), s.end());
}

inline void trim(std::string& s) {
	rtrim(s);
	ltrim(s);
}

struct ExampleAppConsole {
	std::string inputBuf;
	std::vector<std::string> items;
	ImVector<const char*> Commands;
	std::vector<std::string> history;
	int                   HistoryPos = -1;    // -1: new line, 0..History.Size-1 browsing history.
	ImGuiTextFilter       Filter;
	bool                  AutoScroll = true;
	bool                  ScrollToBottom = false;

	ExampleAppConsole() {
		Commands.push_back("help");
		Commands.push_back("history");
		Commands.push_back("clear");	
	}

	// Portable helpers
	static int   Strnicmp(const char* s1, const char* s2, int n) { int d = 0; while (n > 0 && (d = toupper(*s2) - toupper(*s1)) == 0 && *s1) { s1++; s2++; n--; } return d; }

	void draw(const char* title, bool* p_open) {
		ImGui::SetNextWindowSize(ImVec2(520, 600), ImGuiCond_FirstUseEver);

		if (!ImGui::Begin(title, p_open)) {
			ImGui::End();
			return;
		}

		
		
		if (ImGui::SmallButton("Clear")) { items.clear(); }
		ImGui::SameLine();
		bool copy_to_clipboard = ImGui::SmallButton("Copy");
		
		ImGui::Separator();

		// Options menu
		if (ImGui::BeginPopup("Options")) {
			ImGui::Checkbox("Auto-scroll", &AutoScroll);
			ImGui::EndPopup();
		}

		// Options, Filter
		ImGui::SetNextItemShortcut(ImGuiMod_Ctrl | ImGuiKey_O, ImGuiInputFlags_Tooltip);
		if (ImGui::Button("Options"))
			ImGui::OpenPopup("Options");
		ImGui::SameLine();
		Filter.Draw(R"(Filter ("incl,-excl") ("error"))", 180);
		ImGui::Separator();

		// Reserve enough left-over height for 1 separator + 1 input text
		const float footer_height_to_reserve = ImGui::GetStyle().ItemSpacing.y + ImGui::GetFrameHeightWithSpacing();
		if (ImGui::BeginChild("ScrollingRegion", ImVec2(0, -footer_height_to_reserve), ImGuiChildFlags_NavFlattened, ImGuiWindowFlags_HorizontalScrollbar)) {
			if (ImGui::BeginPopupContextWindow()) {
				if (ImGui::Selectable("Clear")) items.clear();
				ImGui::EndPopup();
			}

			ImGui::PushStyleVar(ImGuiStyleVar_ItemSpacing, ImVec2(4, 1)); // Tighten spacing
			if (copy_to_clipboard)
				ImGui::LogToClipboard();
			for (auto const& item : items)
			{
				if (!Filter.PassFilter(item.c_str()))
					continue;

				// Normally you would store more information in your item than just a string.
				// (e.g. make Items[] an array of structure, store color/type etc.)
				ImVec4 color;
				bool has_color = false;
				if (item.starts_with("[error]")) { color = ImVec4(1.0f, 0.4f, 0.4f, 1.0f); has_color = true; }
				else if (item.starts_with("# ")) { color = ImVec4(1.0f, 0.8f, 0.6f, 1.0f); has_color = true; }
				if (has_color)
					ImGui::PushStyleColor(ImGuiCol_Text, color);
				ImGui::TextUnformatted(item.c_str());
				if (has_color)
					ImGui::PopStyleColor();
			}
			if (copy_to_clipboard)
				ImGui::LogFinish();

			// Keep up at the bottom of the scroll region if we were already at the bottom at the beginning of the frame.
			// Using a scrollbar or mouse-wheel will take away from the bottom edge.
			if (ScrollToBottom || (AutoScroll && ImGui::GetScrollY() >= ImGui::GetScrollMaxY()))
				ImGui::SetScrollHereY(1.0f);
			ScrollToBottom = false;

			ImGui::PopStyleVar();
		}
		ImGui::EndChild();
		ImGui::Separator();

		// Command-line
		bool reclaim_focus = false;
		ImGuiInputTextFlags input_text_flags = ImGuiInputTextFlags_EnterReturnsTrue | ImGuiInputTextFlags_EscapeClearsAll | ImGuiInputTextFlags_CallbackCompletion | ImGuiInputTextFlags_CallbackHistory;

		auto callback = [](ImGuiInputTextCallbackData* data) { return static_cast<ExampleAppConsole*>(data->UserData)->TextEditCallback(data); };

		if (ImGui::InputText("Input", &inputBuf, input_text_flags, callback, (void*)this)) {
			trim(inputBuf);
			if (!inputBuf.empty()) exec_command(inputBuf);
			inputBuf.clear();
			reclaim_focus = true;
		}

		ImGui::SetItemDefaultFocus();
		if (reclaim_focus) ImGui::SetKeyboardFocusHere(-1);

		ImGui::End();
	}

	void exec_command(std::string const& command_line) {
		items.push_back(fmt::format("# {}", command_line));

		HistoryPos = -1;

		for (int i = history.size() - 1; i >= 0; i--) {
			if (history[i] == command_line) {
				history.erase(history.begin() + i);
				break;
			}
		}

		history.push_back(command_line);

		if (command_line == "clear") {
			items.clear();
		}
		else if (command_line == "help") {
			items.push_back("Commands:");
			for (int i = 0; i < Commands.Size; i++)
				items.push_back(fmt::format("- {}", Commands[i]));
		}
		else if (command_line == "history") {
			int first = history.size() - 10;

			for (int i = first > 0 ? first : 0; i < history.size(); i++) {
				items.push_back(fmt::format("{}: {}\n", i, history[i]));
			}
		}
		else {
			items.push_back(fmt::format("Unknown command: `{}`\n", command_line));
		}

		ScrollToBottom = true;
	}

	int TextEditCallback(ImGuiInputTextCallbackData* data)
	{
		//AddLog("cursor: %d, selection: %d-%d", data->CursorPos, data->SelectionStart, data->SelectionEnd);
		switch (data->EventFlag)
		{
		case ImGuiInputTextFlags_CallbackCompletion:
		{
			// Example of TEXT COMPLETION

			// Locate beginning of current word
			const char* word_end = data->Buf + data->CursorPos;
			const char* word_start = word_end;
			while (word_start > data->Buf)
			{
				const char c = word_start[-1];
				if (c == ' ' || c == '\t' || c == ',' || c == ';')
					break;
				word_start--;
			}

			// Build a list of candidates
			ImVector<const char*> candidates;
			for (int i = 0; i < Commands.Size; i++)
				if (Strnicmp(Commands[i], word_start, (int)(word_end - word_start)) == 0)
					candidates.push_back(Commands[i]);

			if (candidates.Size == 0)
			{
				// No match
				items.push_back(fmt::format("No match for \"{}\"!\n", std::string_view(word_start, (int)(word_end - word_start))));
			}
			else if (candidates.Size == 1)
			{
				// Single match. Delete the beginning of the word and replace it entirely so we've got nice casing.
				data->DeleteChars((int)(word_start - data->Buf), (int)(word_end - word_start));
				data->InsertChars(data->CursorPos, candidates[0]);
				data->InsertChars(data->CursorPos, " ");
			}
			else
			{
				// Multiple matches. Complete as much as we can..
				// So inputting "C"+Tab will complete to "CL" then display "CLEAR" and "CLASSIFY" as matches.
				int match_len = (int)(word_end - word_start);
				for (;;)
				{
					int c = 0;
					bool all_candidates_matches = true;
					for (int i = 0; i < candidates.Size && all_candidates_matches; i++)
						if (i == 0)
							c = toupper(candidates[i][match_len]);
						else if (c == 0 || c != toupper(candidates[i][match_len]))
							all_candidates_matches = false;
					if (!all_candidates_matches)
						break;
					match_len++;
				}

				if (match_len > 0)
				{
					data->DeleteChars((int)(word_start - data->Buf), (int)(word_end - word_start));
					data->InsertChars(data->CursorPos, candidates[0], candidates[0] + match_len);
				}

				// List matches
				items.emplace_back("Possible matches:\n");
				for (int i = 0; i < candidates.Size; i++)
					items.push_back(fmt::format("- {}\n", candidates[i]));
			}

			break;
		}
		case ImGuiInputTextFlags_CallbackHistory:
		{
			// Example of HISTORY
			const int prev_history_pos = HistoryPos;
			if (data->EventKey == ImGuiKey_UpArrow) {
				if (HistoryPos == -1)
					HistoryPos = history.size() - 1;
				else if (HistoryPos > 0)
					HistoryPos--;
			}
			else if (data->EventKey == ImGuiKey_DownArrow) {
				if (HistoryPos != -1)
					if (++HistoryPos >= history.size())
						HistoryPos = -1;
			}

			// A better implementation would preserve the data on the current input line along with cursor position.
			if (prev_history_pos != HistoryPos) {
				const char* history_str = (HistoryPos >= 0) ? history[HistoryPos].c_str() : "";
				data->DeleteChars(0, data->BufTextLen);
				data->InsertChars(0, history_str);
			}
		}
		}
		return 0;
	}
};

ExampleAppConsole console;

std::string kThumperDirectory;

void tools_binary_search(bool& aOpen) {
	struct Match {
		std::string file;
		std::size_t start;
		std::size_t end;
	};

	struct Result {
		std::string pattern;
		std::vector<Match> matches;
	};

	static std::optional<std::future<Result>> future;

	static std::string input;
	static Result matches;

	// If future ready
	if (future && (future->wait_until(std::chrono::steady_clock::time_point::min()) == std::future_status::ready)) {
		matches = future->get();
		future = std::nullopt;
	}

	if (!aOpen) return;

	static MemoryEditor mem_edit_1;
	mem_edit_1.ReadOnly = true;
	static std::vector<char> file;

	if(ImGui::Begin("Memory Viewer")) {
		ImGui::PushFont(gMonoSpace);
		mem_edit_1.DrawContents(file.data(), file.size());
		ImGui::PopFont();
	}
	ImGui::End();

	if (ImGui::Begin("Binary Search", &aOpen)) {
		ImGui::SetNextItemShortcut(ImGuiMod_Ctrl | ImGuiKey_F);
		if (ImGui::InputText("Input", &input, ImGuiInputTextFlags_EnterReturnsTrue)) {
			if (input.size() > 0 && !future) {
				future = std::async(std::launch::async, [](std::string input) {
					Result result;
					
					std::string parsed = aurora::unescape(input);
					result.pattern = input;

					for (auto const& entry : std::filesystem::directory_iterator(std::filesystem::path(kThumperDirectory) / "cache")) {
						std::ifstream stream;
						stream.open(entry.path(), std::ios::binary | std::ios::in);
						stream.seekg(0, std::ios::end);
						auto size = stream.tellg();
						stream.seekg(0, std::ios::beg);
						std::vector<char> bytes;
						bytes.resize(size);
						stream.read(bytes.data(), bytes.size());
						stream.close();

						auto it = bytes.begin();

						while (true) {
							it = std::search(it, bytes.end(), parsed.begin(), parsed.end());
							if (it == bytes.end()) break;

							result.matches.emplace_back(entry.path().filename().generic_string(), std::distance(bytes.begin(), it), std::distance(bytes.begin(), it) + parsed.size());
							
							++it;
						}
					}

					if(std::filesystem::exists(std::filesystem::path(kThumperDirectory) / "THUMPER_win8.exe.unpacked.exe")){
						std::ifstream stream;
						stream.open(std::filesystem::path(kThumperDirectory) / "THUMPER_win8.exe.unpacked.exe", std::ios::binary | std::ios::in);
						stream.seekg(0, std::ios::end);
						auto size = stream.tellg();
						stream.seekg(0, std::ios::beg);
						std::vector<char> bytes;
						bytes.resize(size);
						stream.read(bytes.data(), bytes.size());
						stream.close();

						auto it = bytes.begin();

						while (true) {
							it = std::search(it, bytes.end(), parsed.begin(), parsed.end());
							if (it == bytes.end()) break;

							result.matches.emplace_back("THUMPER_win8.exe.unpacked.exe", std::distance(bytes.begin(), it), std::distance(bytes.begin(), it) + parsed.size());
							
							++it;
						}
					}

					return result;
				}, input);
			}
		}

		if (future) {
			ImSpinner::SpinnerArcWedges("Spinner", 16.0f);
		}
		else {
			ImGui::SetNextItemShortcut(ImGuiMod_Ctrl | ImGuiKey_C);
			bool copy = ImGui::Button("Copy");

			if (copy) ImGui::LogToClipboard();

			ImGui::Text("%zu results for %s", matches.matches.size(), matches.pattern.c_str());

			for (auto const& match : matches.matches) {

				std::string displayValue = match.file;

				try {
					auto hashValue = std::stoull(match.file.substr(0, match.file.size() - 3), nullptr, 16);
					displayValue = gHashtable.at(static_cast<uint32_t>(hashValue));
					displayValue = aurora::escape(displayValue);
				} catch(std::exception const&) {
				}

				std::string str = fmt::format("{} @ 0x{:x} -> 0x{:x}", displayValue, match.start, match.end);

				if (ImGui::Selectable(str.c_str())) {
					
					std::string path = fmt::format("{}/cache/{}", kThumperDirectory, match.file);

					std::ifstream stream;
					stream.open(path , std::ios::binary | std::ios::in);
					stream.seekg(0, std::ios::end);
					auto size = stream.tellg();
					stream.seekg(0, std::ios::beg);
					file.resize(size);
					stream.read(file.data(), file.size());
					stream.close();

					mem_edit_1.GotoAddrAndHighlight(match.start, match.end);
				}

				
				ImGui::SetItemTooltip("Origin File: %s", match.file.c_str());				
			}

			if (copy) ImGui::LogFinish();
		}
	}
	ImGui::End();
}

void validate_executables() {
	console.items.emplace_back("Validating executables...");

	std::string baseExePath = fmt::format("{}/{}", kThumperDirectory, "THUMPER_win8.exe");
	std::string unpackedExePath = fmt::format("{}/{}", kThumperDirectory, "THUMPER_win8.exe.unpacked.exe");
	
	bool isBaseValid = std::filesystem::exists(baseExePath) && SHA1::from_file(baseExePath) == "d1384dd75cdd3759d95ff02dda32062c148e391e";
	bool isUnpackedValid = std::filesystem::exists(unpackedExePath) && SHA1::from_file(unpackedExePath) == "f125aae1b2dcb16c3fa6db6ebe26a43c1d4f89aa";

	if (!isBaseValid) console.items.emplace_back("[warn] Incorrect hash for base executable");
	if (!isUnpackedValid) console.items.emplace_back("[warn] Incorrect hash for unpacked executable");

}

int lua_print(lua_State* L) {
	console.items.emplace_back(lua_tostring(L, 1));
	return 0;
}

void cache_scan(std::unordered_set<std::string>& pcFileStorage) {
	console.items.emplace_back("Scanning cache...");

	if (kThumperDirectory.empty()) {
		console.items.emplace_back("No thumper path specified, cannot scan cache");
		return;
	}

	pcFileStorage.clear();

	for (auto const& entry : std::filesystem::directory_iterator(std::filesystem::path(kThumperDirectory) / "cache")) {
		pcFileStorage.insert(entry.path().filename().generic_string());
	}

	console.items.push_back(fmt::format("{} file(s)", pcFileStorage.size()));

}

std::unordered_set<std::string> pcFileStorage;

int aurora_rhash(lua_State* L) {
	auto it = gHashtable.find(luaL_checkinteger(L, 1));
	if (it != gHashtable.end()) {
		lua_pushlstring(L, it->second.data(), it->second.size());
		return 1;
	}

	lua_pushnil(L);
	return 1;
}

lua_State* aurora_newstate() {
	lua_State* L = luaL_newstate();

	luaL_openlibs(L);
	lua_register(L, "print", &lua_print);
	aurora::register_plugin_api(L);

	lua_getglobal(L, "Aurora");

	lua_pushcfunction(L, &aurora_rhash);
	lua_setfield(L, -2, "rhash");

	lua_pushcfunction(L, [](lua_State* L)-> int {
		lua_pushboolean(L, pcFileStorage.contains(luaL_checkstring(L, 1)));
		return 1;
	});
	lua_setfield(L, -2, "cache_hit");

	lua_pushcfunction(L, [](lua_State* L)-> int {
		lua_pushstring(L, kThumperDirectory.c_str());
		return 1;
	});
	lua_setfield(L, -2, "game_directory");

	lua_pop(L, 1);

	return L;
}

struct PluginEngine {
	void reload() {
		shutdown();

		L = aurora_newstate();

		if (luaL_dofile(L, "boot.lua") != LUA_OK) {
			console.items.emplace_back(lua_tostring(L, -1));
			shutdown();
			return;
		}

		// Iterate over provided hashtable and store it in the aurora database
		gHashtable.clear();

		if (lua_getfield(L, -1, "hashtable") == LUA_TTABLE) {
			lua_pushnil(L);

			while (lua_next(L, -2)) {
				lua_pushvalue(L, -2);

				std::size_t size;
				char const* data = lua_tolstring(L, -2, &size);

				gHashtable[lua_tointeger(L, -1)] = std::string(data, size);

				lua_pop(L, 2);
			}
		}
		lua_pop(L, 1);
	}

	void update() {
		if (!L) return;

		bool wantReloadPlugins = false;

		// If OnUpdate is provided, invoke it
		if (lua_getfield(L, -1, "OnUpdate") == LUA_TFUNCTION) {
			lua_pcall(L, 0, 0, 0);
		} else lua_pop(L, 1);

		// Build plugin menu
		if (ImGui::BeginMainMenuBar()) {
			if (ImGui::BeginMenu("Plugins")) {
				if (ImGui::MenuItem("Reload Plugins", ImGui::GetKeyChordName(ImGuiMod_Ctrl | ImGuiMod_Shift | ImGuiKey_P))) {
					wantReloadPlugins = true;
				}

				ImGui::Separator();

				if (lua_getfield(L, -1, "plugins") == LUA_TTABLE) {
					lua_pushnil(L);
					while (lua_next(L, -2)) {
						lua_pushvalue(L, -2);
						char const *key = lua_tostring(L, -1);

						lua_getfield(L, -2, "enabled");
						bool const enabled = lua_toboolean(L, -1);
						lua_pop(L, 1);

						if (enabled) {
							if (lua_getfield(L, -2, "gui") == LUA_TTABLE) {
								lua_getfield(L, -1, "visible");
								bool visible = lua_toboolean(L, -1);
								lua_pop(L, 1);

								if (ImGui::MenuItem(key, nullptr, visible)) {
									lua_pushliteral(L, "visible");
									lua_pushboolean(L, !visible); // Flip visible flag
									lua_rawset(L, -3);
								}
							}
							lua_pop(L, 1);
						}

						lua_pop(L, 2);
					}

				}
				lua_pop(L, 1);

				ImGui::EndMenu();
			}

			ImGui::EndMainMenuBar();
		}

		// iterate plugins
		if (lua_getfield(L, -1, "plugins") == LUA_TTABLE) {
			lua_pushnil(L);
			while (lua_next(L, -2)) {
				lua_pushvalue(L, -2);
				char const *key = lua_tostring(L, -1);

				lua_getfield(L, -2, "enabled");
				bool const enabled = lua_toboolean(L, -1);
				lua_pop(L, 1);

				if (enabled) {
					// If a gui is defined for this plugin
					if (lua_getfield(L, -2, "gui") == LUA_TTABLE) {

						lua_getfield(L, -1, "visible");
						bool visible = lua_toboolean(L, -1);
						lua_pop(L, 1);

						lua_getfield(L, -1, "title");
						char const* title = lua_tostring(L, -1);
						lua_pop(L, 1);

						std::string const debugTitle = fmt::format("{} ({})", title, key);

						if (visible) {
							ImGui::SetNextWindowSize(ImVec2(640, 480), ImGuiCond_FirstUseEver);
							if (ImGui::Begin(debugTitle.c_str(), &visible)) {
								if (lua_getfield(L, -1, "OnGui") == LUA_TFUNCTION) {
									if (lua_pcall(L, 0, 0, 0) != LUA_OK) {
										ImGui::TextUnformatted(lua_tostring(L, -1));
										lua_pop(L, 1);
									}
								}
								else lua_pop(L, 1);
							}
							ImGui::End();

							if (!visible) {
								lua_pushliteral(L, "visible");
								lua_pushboolean(L, visible);
								lua_rawset(L, -3);
							}
						}


					}
					lua_pop(L, 1);
				}

				lua_pop(L, 2);
			}

		}
		lua_pop(L, 1);

		if (wantReloadPlugins) {
			reload();
		}
	}

	void shutdown() {
		if (L) lua_close(L);
		L = nullptr;
	}

	lua_State* L = nullptr;
};

bool route_global_shortcut(ImGuiKeyChord const chord) {
	bool const isRouted = ImGui::GetShortcutRoutingData(chord)->RoutingCurr != ImGuiKeyOwner_NoOwner;
	return !isRouted && ImGui::IsKeyChordPressed(chord);
}

void throw_error_box(std::string const& message) {
	tinyfd_messageBox("Critical Error", message.c_str(), "ok", "error", 1);
	throw std::runtime_error(message);
}

namespace aurora {
void main() {
	PluginEngine pluginEngine;
	pluginEngine.reload();

	// Load configs
	{
		console.items.emplace_back("Loading aurora config");

		lua_State* L = aurora_newstate();

		if (luaL_dofile(L, "config.lua") != LUA_OK) {
			console.items.push_back(fmt::format("Lua Error: {}", lua_tostring(L, -1)));
		}
		else {
			if (!lua_istable(L, -1)) {
				console.items.emplace_back("Invalid config. Use `Tools > Set Thumper Path` to repair");
			}
			else {
				if (lua_getfield(L, -1, "path") == LUA_TSTRING) {
					kThumperDirectory = lua_tostring(L, -1);
				}
				else {
					console.items.emplace_back("Invalid config. Use `Tools > Set Thumper Path` to repair");
				}
				lua_pop(L, 1);
			}
		}
		lua_pop(L, 1);

		lua_close(L);

		console.items.emplace_back("Validating aurora config");

		if (!std::filesystem::exists(kThumperDirectory)) {
			console.items.emplace_back("Specified path doesn't exist. Use `Tools > Set Thumper Path` to repair");
			kThumperDirectory = "";
		}
	}

	bool toolsBinarySearch = false;
	bool open = true;
	
	validate_executables();
	cache_scan(pcFileStorage);

	glfwSetErrorCallback([](int errorCode, const char *description) {
		std::cerr << description << '\n';
	});

	if (!glfwInit()) throw_error_box("Failed to initialize GLFW");

	glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
	glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
	glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
	glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
	glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

	GLFWwindow* window = glfwCreateWindow(1280, 720, "Aurora v0.0.4-a.4", nullptr, nullptr);
	if (!window) throw_error_box("Failed to create GLFW window");

	glfwMakeContextCurrent(window);
	if (!gladLoadGL(&glfwGetProcAddress)) throw_error_box("Failed to initialize GLAD");

	IMGUI_CHECKVERSION();
	ImGui::CreateContext();
	ImGuiIO& io = ImGui::GetIO();
	io.ConfigFlags |= ImGuiConfigFlags_NavEnableKeyboard;
	io.ConfigFlags |= ImGuiConfigFlags_DockingEnable;

	gVariableSpace = io.Fonts->AddFontFromFileTTF("NotoSans-Regular.ttf", 18.0f);
	gMonoSpace = io.Fonts->AddFontFromFileTTF("NotoSansMono-Regular.ttf", 18.0f);

	ImGui::StyleColorsDark();
	ImGui_ImplGlfw_InitForOpenGL(window, true);
	ImGui_ImplOpenGL3_Init("#version 450 core");

	console.items.emplace_back("Welcome to Aurora!");

	while(!glfwWindowShouldClose(window)) {
		glfwPollEvents();

		ImGui_ImplOpenGL3_NewFrame();
		ImGui_ImplGlfw_NewFrame();
		ImGui::NewFrame();

		ImGui::DockSpaceOverViewport();

		if (ImGui::BeginMainMenuBar()) {
			if (ImGui::BeginMenu("Tools")) {
				if (ImGui::MenuItem("Set Thumper Path")) {
					char const* pattern = "THUMPER_*.exe";
					char* result = tinyfd_openFileDialog("Select thumper executable", nullptr, 1, &pattern, nullptr, false);
					if (result) {
						std::string path = std::filesystem::path(result).parent_path().generic_string();

						if (std::filesystem::exists(path)) {
							console.items.emplace_back("New thumper directory specified, validating");
							kThumperDirectory = path;
							validate_executables();
							cache_scan(pcFileStorage);

							console.items.emplace_back("Validation complete, writing to config");

							std::string luaString;

							lua_State* L = luaL_newstate();
							luaL_openlibs(L);
							lua_getglobal(L, "string");
							lua_getfield(L, -1, "format");
							lua_pushliteral(L, "%q");
							lua_pushstring(L, path.c_str());
							lua_pcall(L, 2, 1, 0);
							luaString = lua_tostring(L, -1);
							lua_close(L);

							std::ofstream stream;
							stream.open("config.lua", std::ios::out);
							stream << "return {\n\tpath = ";
							stream << luaString;
							stream << "\n}";
							stream.close();
						}
						else {
							console.items.emplace_back("Specified path doesn't exist. Operation stopped");
						}
					}
				}
				
				ImGui::MenuItem("Binary Search", nullptr, &toolsBinarySearch, !kThumperDirectory.empty());
				

				ImGui::EndMenu();
			}
		}

		ImGui::EndMainMenuBar();

		if (route_global_shortcut(ImGuiMod_Ctrl | ImGuiMod_Shift | ImGuiKey_P)) {
			pluginEngine.reload();
		}

		pluginEngine.update();

		tools_binary_search(toolsBinarySearch);

		console.draw("Console", &open);

		ImGui::Render();
		int display_w, display_h;
		glfwGetFramebufferSize(window, &display_w, &display_h);
		glViewport(0, 0, display_w, display_h);
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
		ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());

		glfwSwapBuffers(window);
	}

	pluginEngine.shutdown();

	ImGui_ImplOpenGL3_Shutdown();
	ImGui_ImplGlfw_Shutdown();
	ImGui::DestroyContext();

	glfwMakeContextCurrent(nullptr);
	glfwDestroyWindow(window);
	glfwTerminate();
}
}