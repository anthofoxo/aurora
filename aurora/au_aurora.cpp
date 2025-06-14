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

#include "icon.hpp"

#define STB_IMAGE_IMPLEMENTATION
#include "stb_image.h"

#include "au_util.hpp"



ImFont* gVariableSpace = nullptr;
ImFont* gMonoSpace = nullptr;

std::unordered_map<std::uint32_t, std::string> gHashtable;

struct Console {
	std::vector<std::string> mItems;
	bool mAutoScroll = true;
	bool mScrollToBottom = false;

	void log(std::string &&item) { mItems.push_back(std::move(item)); }
	void log(std::string const &item) { mItems.push_back(item); }
	void clear() { mItems.clear(); }

	void draw(const char* title, bool* p_open) {
		ImGui::SetNextWindowSize(ImVec2(520, 600), ImGuiCond_FirstUseEver);

		if (!ImGui::Begin(title, p_open)) {
			ImGui::End();
			return;
		}
		
		if (ImGui::SmallButton("Clear")) clear();
		ImGui::SameLine();
		bool copy_to_clipboard = ImGui::SmallButton("Copy");
		
		ImGui::Separator();

		const float footer_height_to_reserve = ImGui::GetStyle().ItemSpacing.y + ImGui::GetFrameHeightWithSpacing();
		if (ImGui::BeginChild("ScrollingRegion", ImVec2(0, -footer_height_to_reserve), ImGuiChildFlags_NavFlattened, ImGuiWindowFlags_HorizontalScrollbar)) {
			if (ImGui::BeginPopupContextWindow()) {
				if (ImGui::Selectable("Clear")) clear();
				ImGui::EndPopup();
			}

			ImGui::PushStyleVar(ImGuiStyleVar_ItemSpacing, ImVec2(4, 1)); // Tighten spacing
			if (copy_to_clipboard) ImGui::LogToClipboard();

			for (auto const& item : mItems) {

				// Normally you would store more information in your item than just a string.
				// (e.g. make Items[] an array of structure, store color/type etc.)
				ImVec4 color;
				bool has_color = false;
				if (item.starts_with("[error]")) { color = ImVec4(1.0f, 0.4f, 0.4f, 1.0f); has_color = true; }
				else if (item.starts_with("# ")) { color = ImVec4(1.0f, 0.8f, 0.6f, 1.0f); has_color = true; }

				if (has_color) ImGui::PushStyleColor(ImGuiCol_Text, color);
				ImGui::TextUnformatted(item.c_str());
				if (has_color) ImGui::PopStyleColor();
			}

			if (copy_to_clipboard) ImGui::LogFinish();

			// Keep up at the bottom of the scroll region if we were already at the bottom at the beginning of the frame.
			// Using a scrollbar or mouse-wheel will take away from the bottom edge.
			if (mScrollToBottom || (mAutoScroll && ImGui::GetScrollY() >= ImGui::GetScrollMaxY()))
				ImGui::SetScrollHereY(1.0f);
			mScrollToBottom = false;

			ImGui::PopStyleVar();
		}
		ImGui::EndChild();

		ImGui::End();
	}
};

Console console;

std::string kThumperDirectory;

struct HexEditor {
	MemoryEditor mMemoryEditor;
	std::vector<std::byte> mData;
	std::string mPathPreview;

	void action_loadfile(std::filesystem::path const& aPath) {
		if (auto data = aurora::read_file(aPath); data.has_value()) {
			mPathPreview = aPath.string();
			mData = std::move(data.value());
		}
	}

	void action_jump(std::size_t start, std::size_t end) {
		mMemoryEditor.GotoAddrAndHighlight(start, end);
	}

	void draw() {
		std::string const label = fmt::format("Hex Editor {}###Hex Editor", mPathPreview);

		if (ImGui::Begin(label.c_str())) {
			ImGui::PushFont(gMonoSpace);
			mMemoryEditor.DrawContents(mData.data(), mData.size());
			ImGui::PopFont();
		}
		ImGui::End();
	}
};

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
	if (future && aurora::is_future_ready(*future)) {
		matches = future->get();
		future = std::nullopt;
	}

	if (!aOpen) return;

	static HexEditor hexEditor;
	hexEditor.draw();

	if (ImGui::Begin("Binary Search", &aOpen)) {
		ImGui::SetNextItemShortcut(ImGuiMod_Ctrl | ImGuiKey_F);
		if (ImGui::InputText("Input", &input, ImGuiInputTextFlags_EnterReturnsTrue)) {
			if (!input.empty() && !future) {
				future = std::async(std::launch::async, [](std::string input) {
					Result result;
					
					std::string parsed = aurora::unescape(input);
					auto parsedSpan = std::as_bytes(std::span(parsed));
					result.pattern = input;

					for (auto const& entry : std::filesystem::directory_iterator(std::filesystem::path(kThumperDirectory) / "cache")) {
						if (auto const data = aurora::read_file(entry.path())) {
							auto it = data->begin();

							while (true) {
								it = std::search(it, data->end(), parsedSpan.begin(), parsedSpan.end());
								if (it == data->end()) break;

								result.matches.emplace_back(entry.path().filename().generic_string(), std::distance(data->begin(), it), std::distance(data->begin(), it) + parsedSpan.size());

								++it;
							}
						}
					}

					if(std::filesystem::exists(std::filesystem::path(kThumperDirectory) / "THUMPER_win8.exe.unpacked.exe")){
						auto const data = aurora::read_file(std::filesystem::path(kThumperDirectory) / "THUMPER_win8.exe.unpacked.exe");

						if (data.has_value()) {
							auto it = data->begin();

							while (true) {
								it = std::search(it, data->end(), parsedSpan.begin(), parsedSpan.end());
								if (it == data->end()) break;

								result.matches.emplace_back("THUMPER_win8.exe.unpacked.exe", std::distance(data->begin(), it), std::distance(data->begin(), it) + parsed.size());

								++it;
							}
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

					hexEditor.action_loadfile(path);
					hexEditor.action_jump(match.start, match.end);
				}

				
				ImGui::SetItemTooltip("Origin File: %s", match.file.c_str());
			}

			if (copy) ImGui::LogFinish();
		}
	}
	ImGui::End();
}

void validate_executables() {
	console.log("Validating executables...");

	std::string baseExePath = fmt::format("{}/{}", kThumperDirectory, "THUMPER_win8.exe");
	std::string unpackedExePath = fmt::format("{}/{}", kThumperDirectory, "THUMPER_win8.exe.unpacked.exe");
	
	bool isBaseValid = std::filesystem::exists(baseExePath) && SHA1::from_file(baseExePath) == "d1384dd75cdd3759d95ff02dda32062c148e391e";
	bool isUnpackedValid = std::filesystem::exists(unpackedExePath) && SHA1::from_file(unpackedExePath) == "f125aae1b2dcb16c3fa6db6ebe26a43c1d4f89aa";

	if (!isBaseValid) console.log("[warn] Incorrect hash for base executable");
	if (!isUnpackedValid) console.log("[warn] Incorrect hash for unpacked executable");

}

int lua_print(lua_State* L) {
	console.log(lua_tostring(L, 1));
	return 0;
}

void cache_scan(std::unordered_set<std::string>& pcFileStorage) {
	console.log("Scanning cache...");

	if (kThumperDirectory.empty()) {
		console.log("No thumper path specified, cannot scan cache");
		return;
	}

	pcFileStorage.clear();

	for (auto const& entry : std::filesystem::directory_iterator(std::filesystem::path(kThumperDirectory) / "cache")) {
		pcFileStorage.insert(entry.path().filename().generic_string());
	}

	console.log(fmt::format("{} file(s)", pcFileStorage.size()));

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
			console.log(lua_tostring(L, -1));
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

		// Build plugin menu
		if (ImGui::BeginMainMenuBar()) {
			if (ImGui::BeginMenu("Plugins")) {
				if (ImGui::MenuItem("Reload Plugins", ImGui::GetKeyChordName(ImGuiMod_Ctrl | ImGuiMod_Shift | ImGuiKey_R))) {
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
								bool const visible = lua_toboolean(L, -1);
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
		if (!L) return;

		// For all plugins with a OnUnload function defined, invoke it
		if (lua_getfield(L, -1, "plugins") == LUA_TTABLE) {
			lua_pushnil(L);
			while (lua_next(L, -2)) {
				lua_pushvalue(L, -2);

				lua_getfield(L, -2, "enabled");
				bool const enabled = lua_toboolean(L, -1);
				lua_pop(L, 1);

				if (enabled) {
					if (lua_getfield(L, -2, "OnUnload") == LUA_TFUNCTION) {
						lua_pcall(L, 0, 0, 0);
					}
					else {
						lua_pop(L, 1);
					}
				}

				lua_pop(L, 2);
			}

		}
		lua_pop(L, 1);

		lua_close(L);
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
	// Load configs
	{
		console.log("Loading aurora config");

		lua_State* L = aurora_newstate();

		if (luaL_dofile(L, "config.lua") != LUA_OK) {
			console.log(fmt::format("Lua Error: {}", lua_tostring(L, -1)));
		}
		else {
			if (!lua_istable(L, -1)) {
				console.log("Invalid config. Use `Tools > Set Thumper Path` to repair");
			}
			else {
				if (lua_getfield(L, -1, "path") == LUA_TSTRING) {
					kThumperDirectory = lua_tostring(L, -1);
				}
				else {
					console.log("Invalid config. Use `Tools > Set Thumper Path` to repair");
				}
				lua_pop(L, 1);
			}
		}
		lua_pop(L, 1);

		lua_close(L);

		console.log("Validating aurora config");

		if (!std::filesystem::exists(kThumperDirectory)) {
			console.log("Specified path doesn't exist. Use `Tools > Set Thumper Path` to repair");
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

	GLFWwindow* window = glfwCreateWindow(1280, 720, "Aurora v0.0.4-a.5", nullptr, nullptr);
	if (!window) throw_error_box("Failed to create GLFW window");

	GLFWimage image;
	image.pixels = stbi_load_from_memory(aurora::icon_data, aurora::icon_size, &image.width, &image.height, nullptr, 4);
	glfwSetWindowIcon(window, 1, &image);
	stbi_image_free(image.pixels);

	glfwMakeContextCurrent(window);
	if (!gladLoadGL(&glfwGetProcAddress)) throw_error_box("Failed to initialize GLAD");

	PluginEngine pluginEngine;
	pluginEngine.reload();

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

	console.log("Welcome to Aurora!");

	bool showDemo = false;

	while(!glfwWindowShouldClose(window)) {
		glfwPollEvents();

		ImGui_ImplOpenGL3_NewFrame();
		ImGui_ImplGlfw_NewFrame();
		ImGui::NewFrame();

		ImGui::DockSpaceOverViewport();

		if (ImGui::BeginMainMenuBar()) {
			if (ImGui::BeginMenu("View")) {
				ImGui::MenuItem("Console", nullptr, &open);
				ImGui::Separator();
				ImGui::MenuItem("Dear ImGui Demo", ImGui::GetKeyChordName(ImGuiMod_Ctrl | ImGuiMod_Shift | ImGuiKey_D), &showDemo);
				ImGui::EndMenu();
			}

			if (ImGui::BeginMenu("Tools")) {
				if (ImGui::MenuItem("Set Thumper Path")) {
					char const* pattern = "THUMPER_*.exe";
					char* result = tinyfd_openFileDialog("Select thumper executable", nullptr, 1, &pattern, nullptr, false);
					if (result) {
						std::string path = std::filesystem::path(result).parent_path().generic_string();

						if (std::filesystem::exists(path)) {
							console.log("New thumper directory specified, validating");
							kThumperDirectory = path;
							validate_executables();
							cache_scan(pcFileStorage);

							console.log("Validation complete, writing to config");

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
							console.log("Specified path doesn't exist. Operation stopped");
						}
					}
				}
				
				ImGui::MenuItem("Binary Search", nullptr, &toolsBinarySearch, !kThumperDirectory.empty());
				

				ImGui::EndMenu();
			}
		}

		ImGui::EndMainMenuBar();

		if (showDemo) {
			ImGui::ShowDemoWindow(&showDemo);
		}

		if (route_global_shortcut(ImGuiMod_Ctrl | ImGuiMod_Shift | ImGuiKey_D)) {
			showDemo ^= true;
		}

		if (route_global_shortcut(ImGuiMod_Ctrl | ImGuiMod_Shift | ImGuiKey_R)) {
			pluginEngine.reload();
		}

		pluginEngine.update();

		tools_binary_search(toolsBinarySearch);

		if (open) {
			console.draw("Console", &open);
		}

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
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