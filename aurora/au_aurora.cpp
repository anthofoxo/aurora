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

#include "lua_api.hpp"

#include <lua.hpp>
#include <spdlog/fmt/fmt.h>
#include <tinyfiledialogs.h>

#include <unordered_map>
#include <iostream>
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
#include <format>

#define STB_IMAGE_IMPLEMENTATION
#include "stb_image.h"
#define STB_IMAGE_RESIZE_IMPLEMENTATION
#include "stb_image_resize2.h"

#include "au_util.hpp"
#include "spdlog/spdlog.h"

#include "au_hash.hpp"
#include "au_lua_serialize.hpp"
#include "gui/au_hasher.hpp"

struct ByteStream final {
	char const* const kError = "ByteStream out of range";

	std::vector<std::byte> mBuffer;
	std::size_t mOffset = 0;

	template<typename T>
	T read_gen() {
		if (mOffset + sizeof(T) > mBuffer.size()) throw std::out_of_range(kError);
		T value;
		std::memcpy(&value, mBuffer.data() + mOffset, sizeof(T));
		mOffset += sizeof(T);
		return value;
	}

	bool read_bool() { return read_gen<std::uint8_t>(); }
	std::uint32_t read_u8() { return read_gen<std::uint8_t>(); }
	std::uint32_t read_u32() { return read_gen<std::uint32_t>(); }

	std::string read_sstr() {
		auto size = read_u32();
		if (mOffset + size > mBuffer.size()) throw std::out_of_range(kError);
		std::string string = std::string(reinterpret_cast<char const*>(mBuffer.data() + mOffset), size);
		mOffset += size;
		return string;
	}

	std::string read_cstr() {
		// We cannot guarentee safety with a c string
		std::size_t size = std::strlen(reinterpret_cast<char const*>(mBuffer.data() + mOffset));
		std::string string = std::string(reinterpret_cast<char const*>(mBuffer.data() + mOffset), size);
		mOffset += size;
		mOffset += 1; // Null terminator
		return string;
	}

	template<typename T>
	void write_gen(T const& value) {
		for (int i = 0; i < sizeof(T); ++i) mBuffer.emplace_back(static_cast<std::byte>(0));
		std::memcpy(mBuffer.data() + mBuffer.size() - sizeof(T), &value, sizeof(T));
	}

	void write_bool(bool value) { return write_gen(value); }
	void write_u8(std::uint8_t value) { return write_gen(value); }
	void write_u32(std::uint32_t value) { return write_gen(value); }

	void write_sstr(std::string const& value) {
		write_u32(value.size());
		for (auto c : value) mBuffer.emplace_back(static_cast<std::byte>(c));
	}

	void write_cstr(std::string const& value) {
		for (auto c : value) mBuffer.emplace_back(static_cast<std::byte>(c));
		mBuffer.emplace_back(static_cast<std::byte>('\0'));
	}
};

void write_to_thumper_cache(std::uint32_t hash, std::span<std::byte const> bytes) {
	std::string path = std::format("cache/{:x}.pc", hash);

	if (std::filesystem::exists(path) && !std::filesystem::exists(path + ".bak")) {
		std::filesystem::copy_file(path, path + ".bak");
	}

	std::ofstream stream(path, std::ios::binary);
	if (!stream) {
		__debugbreak();
	}
	stream.write(reinterpret_cast<char const*>(bytes.data()), bytes.size());
}

bool cache_file_exists(std::uint32_t value) {
	return std::filesystem::exists(std::format("cache/{:x}.pc", value));
}

struct Credits {
	struct MajorGroupElement {
		std::string decoration;
		std::string text;

		void deserialize(ByteStream& stream) {
			decoration = stream.read_sstr();
			text = stream.read_sstr();
		}

		void serialize(ByteStream& stream) const {
			stream.write_sstr(decoration);
			stream.write_sstr(text);
		}

		void serialize(lua_State* L) {
			lua_newtable(L);
			lua_pushstring(L, decoration.c_str());
			lua_setfield(L, -2, "decoration");
			lua_pushstring(L, text.c_str());
			lua_setfield(L, -2, "text");
			// one new value added to stack
		}

		void deserialize(lua_State* L) {
			lua_getfield(L, -1, "decoration");
			decoration = lua_tostring(L, -1);
			lua_pop(L, 1);

			lua_getfield(L, -1, "text");
			text = lua_tostring(L, -1);
			lua_pop(L, 1);
		}
	};

	struct MajorGroup {
		std::vector<MajorGroupElement> elements;

		void deserialize(ByteStream& stream) {
			elements.resize(stream.read_u32());

			for (auto& element : elements) {
				element.deserialize(stream);
			}
		}

		void serialize(ByteStream& stream) const {
			stream.write_u32(elements.size());

			for (auto const& element : elements) {
				element.serialize(stream);
			}
		}

		void serialize(lua_State* L) {
			lua_newtable(L);

			for (int i = 0; i < elements.size(); ++i) {
				elements[i].serialize(L);
				lua_rawseti(L, -2, i + 1);
			}
		}

		void deserialize(lua_State* L) {
			elements.clear();

			lua_pushnil(L);
			while (lua_next(L, -2)) {
				MajorGroupElement element;
				element.deserialize(L);

				elements.emplace_back(std::move(element));

				lua_pop(L, 1);
			}
		}
	};

	struct MinorGroup {
		std::string banner;
		std::vector<std::string> thanks;

		void deserialize(ByteStream& stream) {
			banner = stream.read_sstr();
			thanks.resize(stream.read_u32());

			for (auto& str : thanks) {
				str = stream.read_sstr();
			}
		}

		void serialize(ByteStream& stream) const {
			stream.write_sstr(banner);
			stream.write_u32(thanks.size());

			for (auto& str : thanks) {
				stream.write_sstr(str);
			}
		}

		void serialize(lua_State* L) {
			lua_newtable(L);
			lua_pushstring(L, banner.c_str());
			lua_setfield(L, -2, "banner");

			lua_newtable(L);
			for (int i = 0; i < thanks.size(); ++i) {
				lua_pushstring(L, thanks[i].c_str());
				lua_rawseti(L, -2, i + 1);
			}

			lua_setfield(L, -2, "elements");
		}

		void deserialize(lua_State* L) {
			thanks.clear();

			lua_getfield(L, -1, "banner");
			banner = lua_tostring(L, -1);
			lua_pop(L, 1);

			lua_getfield(L, -1, "elements");

			lua_pushnil(L);
			while (lua_next(L, -2)) {
				thanks.emplace_back(lua_tostring(L, -1));

				lua_pop(L, 1);
			}

			lua_pop(L, 1);
		}
	};

	std::vector<MajorGroup> major;
	std::vector<MinorGroup> minor;

	void deserialize(ByteStream& stream) {
		major.resize(stream.read_u32());

		for (auto& group : major) {
			group.deserialize(stream);
		}

		minor.resize(stream.read_u32());

		for (auto& group : minor) {
			group.deserialize(stream);
		}
	}

	void serialize(ByteStream& stream) const {
		stream.write_u32(major.size());

		for (auto& group : major) {
			group.serialize(stream);
		}

		stream.write_u32(minor.size());

		for (auto& group : minor) {
			group.serialize(stream);
		}
	}

	void serialize(lua_State* L) {
		lua_newtable(L);

		lua_newtable(L);
		for (int i = 0; i < major.size(); ++i) {
			major[i].serialize(L);
			lua_rawseti(L, -2, i + 1);
		}
		lua_setfield(L, -2, "main");

		lua_newtable(L);
		for (int i = 0; i < minor.size(); ++i) {
			minor[i].serialize(L);
			lua_rawseti(L, -2, i + 1);
		}
		lua_setfield(L, -2, "tail");
	}

	void deserialize(lua_State* L) {
		major.clear();
		minor.clear();

		lua_getfield(L, -1, "main");

		lua_pushnil(L);
		while (lua_next(L, -2)) {
			MajorGroup group;
			group.deserialize(L);
			major.emplace_back(std::move(group));

			lua_pop(L, 1);
		}

		lua_pop(L, 1);
		lua_getfield(L, -1, "tail");
		lua_pushnil(L);
		while (lua_next(L, -2)) {
			MinorGroup group;
			group.deserialize(L);
			minor.emplace_back(std::move(group));

			lua_pop(L, 1);
		}

		lua_pop(L, 1);
	}
};

struct LocalizationEntry {
	std::uint32_t key;
	std::string value;
	std::uint32_t offset;



	void serialize(lua_State* L) {
		std::unordered_map<std::uint32_t, std::string> map;
#define INSERT_HASH(x) map[aurora::fnv1a(std::span(std::string_view(x)))] = x

		INSERT_HASH("level1");
		INSERT_HASH("level2");
		INSERT_HASH("level3");
		INSERT_HASH("level4");
		INSERT_HASH("level5");
		INSERT_HASH("level6");
		INSERT_HASH("level7");
		INSERT_HASH("level8");
		INSERT_HASH("level9");
		INSERT_HASH("accept");
		INSERT_HASH("no");
		INSERT_HASH("cancel");
		INSERT_HASH("continue");
		INSERT_HASH("play");
		INSERT_HASH("yes");
		INSERT_HASH("leaderboard_view");
		INSERT_HASH("rank");
		INSERT_HASH("retry");
		INSERT_HASH("tip");

		auto it = map.find(key);
		if (it != map.end()) {
			lua_pushstring(L, it->second.c_str());
		}
		else {
			lua_pushinteger(L, key);
		}


		lua_pushstring(L, value.c_str());
		lua_rawset(L, -3);
	}
};

struct Localization final {
	std::vector<LocalizationEntry> enteries;

	void deserialize(ByteStream& stream) {
		auto cstrCount = stream.read_u32();
		auto byteCount = stream.read_u32();

		enteries.resize(cstrCount);

		for (int i = 0; i < cstrCount; ++i) {
			enteries[i].value = stream.read_cstr();
		}

		for (int i = 0; i < cstrCount; ++i) {
			enteries[i].key = stream.read_u32();
			enteries[i].offset = stream.read_u32();
		}
	}

	void serialize(lua_State* L) {
		lua_newtable(L);
		for (int i = 0; i < enteries.size(); ++i) {
			enteries[i].serialize(L);
		}
	}
};


void unpack_localization() {
	lua_State* L = luaL_newstate();

	if (luaL_dofile(L, "aurora/localization.lua") == LUA_OK) {

		lua_pushnil(L);
		while (lua_next(L, -2)) {
			std::size_t size;
			char const* data = lua_tolstring(L, -1, &size);
			auto hash = aurora::fnv1a(data, size);

			if (auto bytes = aurora::read_file(std::format("cache/{:x}.pc", hash))) {
				ByteStream stream;
				stream.mBuffer = std::move(*bytes);

				lua_State* L = luaL_newstate();
				luaL_openlibs(L);

				stream.read_u32(); // ignore header // 16
				Localization locs;
				locs.deserialize(stream);
				locs.serialize(L);

				std::string readyToWrite = std::string("return ") + aurora::lapi_serialize(L);

				std::string writePath = std::format("mods/base/localization/{}.lua", std::string(data + 1, size - 5));
				std::filesystem::create_directories(std::filesystem::path(writePath).parent_path());

				std::ofstream s(writePath, std::ios::binary);
				s.write(readyToWrite.data(), readyToWrite.size());
				s.close();
			}

			lua_pop(L, 1);
		}

	}
	lua_pop(L, 1);

	lua_close(L);
}

void unpack_credits() {
	lua_State* L = luaL_newstate();

	if (luaL_dofile(L, "aurora/credits.lua") == LUA_OK) {

		lua_pushnil(L);
		while (lua_next(L, -2)) {
			std::size_t size;
			char const* data = lua_tolstring(L, -1, &size);
			auto hash = aurora::fnv1a(data, size);

			if (auto bytes = aurora::read_file(std::format("cache/{:x}.pc", hash))) {
				ByteStream stream;
				stream.mBuffer = std::move(*bytes);

				lua_State* L = luaL_newstate();
				luaL_openlibs(L);

				stream.read_u32();
				Credits credits;
				credits.deserialize(stream);
				credits.serialize(L);

				std::string readyToWrite = std::string("return ") + aurora::lapi_serialize(L);

				std::string writePath = std::format("mods/base/credits/{}.lua", std::string(data + 1, size - 9));
				std::filesystem::create_directories(std::filesystem::path(writePath).parent_path());

				std::ofstream s(writePath, std::ios::binary);
				s.write(readyToWrite.data(), readyToWrite.size());
				s.close();
			}

			lua_pop(L, 1);
		}

	}
	lua_pop(L, 1);

	lua_close(L);
}

// Texture unpacking is easy, iterate over the known texture hashes, compute its .pc filename, strip the first 4 bytes and export it as .dds (ideally this would be a .png)
void unpack_textures() {
	lua_State* L = luaL_newstate();

	if (luaL_dofile(L, "aurora/textures.lua") == LUA_OK) {

		lua_pushnil(L);
		while (lua_next(L, -2)) {
			std::size_t size;
			char const* data = lua_tolstring(L, -1, &size);
			auto hash = aurora::fnv1a(data, size);

			bool isEngineData = *data == 'E';

			std::uint32_t extra;
			std::memcpy(&extra, data + size - 4, 4);

			// read computed name
			if (auto bytes = aurora::read_file(std::format("cache/{:x}.pc", hash))) {
				ByteStream stream;
				stream.mBuffer = std::move(*bytes);

				stream.read_u32(); // skip first u32

				std::string engineDir;

				if (isEngineData) {
					engineDir = "engine/";
				}

				std::string writePath = std::format("mods/base/textures/{}{}+{:x}.dds", engineDir, std::string(data + 1, size - 9), extra);
				std::filesystem::create_directories(std::filesystem::path(writePath).parent_path());

				std::ofstream s(writePath, std::ios::binary);
				s.write(reinterpret_cast<char const*>(stream.mBuffer.data()) + 4, stream.mBuffer.size() - 4);
				s.close();
			}

			lua_pop(L, 1);
		}

	}
	lua_pop(L, 1);

	lua_close(L);
}

void unpack_gui(bool& visible) {

	if (!visible) return;
	if (ImGui::Begin("Unpacker")) {
		ImGui::TextWrapped("%s", "Make sure the backup files are restored before doing this");

		if (ImGui::Button("Unpack Credits")) {
			unpack_credits();
		}

		if (ImGui::Button("Unpack Textures")) {
			unpack_textures();
		}

		if (ImGui::Button("Unpack Localization")) {
			unpack_localization();
		}
	}
	ImGui::End();
}


enum struct LocalizationKey : std::uint32_t {
	kPlay = aurora::fnv1a(aurora::Context::kConsteval, "play"),
	kCancel = aurora::fnv1a(aurora::Context::kConsteval, "cancel"),
	kNo = aurora::fnv1a(aurora::Context::kConsteval, "no"),
	kContinue = aurora::fnv1a(aurora::Context::kConsteval, "continue"),
};

std::vector<std::string> gBuildMessages;
std::mutex gBuildMessagesMtx;

void post_build_message(std::string const& string) {
	std::lock_guard lck{ gBuildMessagesMtx };
	gBuildMessages.emplace_back(string);
}

template <typename... Types>
void post_build_message(std::format_string<Types...> const fmt, Types&&... args) {
	post_build_message(std::format(fmt, std::forward<Types>(args)...));
};

struct ModDb {
	std::unordered_map<std::string, std::unordered_map<LocalizationKey, std::string>> localization;
	std::unordered_map<std::string, Credits> credits;

	std::unordered_map<std::string, std::string> textures; // maps the texture name to the texture target
};

ModDb gModDb;

struct ModEntry {
	std::string modid;
	bool enabled;
};

static std::vector<ModEntry> gFoundMods;


void process_mod_hooks(std::string const& modid) {
	post_build_message("Processing patches `{}`", modid);

	if (std::filesystem::exists(std::format("mods/{}/patches/credits", modid))) {
		for (auto const& entry : std::filesystem::recursive_directory_iterator(std::format("mods/{}/patches/credits", modid))) {
			if (entry.is_directory()) continue;
			if (entry.path().extension().generic_string() != ".lua") continue;

			post_build_message(entry.path().generic_string());

			lua_State* L = luaL_newstate();
			luaL_openlibs(L);

			std::filesystem::path fspath = std::filesystem::relative(entry.path(), std::format("mods/{}/patches/credits", modid)).generic_string();
			std::string path = std::format("A{}/{}.credits", fspath.parent_path().generic_string(), fspath.stem().generic_string());

			gModDb.credits[path].serialize(L);

			if (luaL_dofile(L, entry.path().generic_string().c_str()) == LUA_OK) {
				lua_pushvalue(L, -2);
				lua_pcall(L, 1, 0, 0);
				gModDb.credits[path].deserialize(L);
			}
			else {
				post_build_message(lua_tostring(L, -1));
			}
			lua_pop(L, 1);
			lua_close(L);
		}
	}
}

void load_mod(std::string const& modid) {
	post_build_message("Loading `{}`", modid);

	if (std::filesystem::exists(std::format("mods/{}/localization", modid))) {
		for (auto const& entry : std::filesystem::recursive_directory_iterator(std::format("mods/{}/localization", modid))) {
			if (entry.is_directory()) continue;
			if (entry.path().extension().generic_string() != ".lua") continue;

			post_build_message(entry.path().generic_string());

			lua_State* L = luaL_newstate();
			if (luaL_dofile(L, entry.path().generic_string().c_str()) == LUA_OK) {
				std::filesystem::path fspath = std::filesystem::relative(entry.path(), std::format("mods/{}/localization", modid)).generic_string();
				std::string path = std::format("A{}/{}.loc", fspath.parent_path().generic_string(), fspath.stem().generic_string());

				lua_pushnil(L);
				while (lua_next(L, -2)) {
					lua_pushvalue(L, -2);

					LocalizationKey localizationKey;

					if (lua_type(L, -1) == LUA_TSTRING) {
						std::size_t size;
						char const* data = lua_tolstring(L, -1, &size);
						localizationKey = LocalizationKey(aurora::fnv1a(data, size));
					}
					else {
						localizationKey = LocalizationKey(lua_tointeger(L, -1));
					}

					gModDb.localization[path][localizationKey] = std::string(lua_tostring(L, -2));

					lua_pop(L, 2);
				}
			}
			else {
				post_build_message(lua_tostring(L, -1));
			}
			lua_pop(L, 1);
			lua_close(L);
		}
	}

	if (std::filesystem::exists(std::format("mods/{}/credits", modid))) {
		for (auto const& entry : std::filesystem::recursive_directory_iterator(std::format("mods/{}/credits", modid))) {
			if (entry.is_directory()) continue;
			if (entry.path().extension().generic_string() != ".lua") continue;

			post_build_message(entry.path().generic_string());

			lua_State* L = luaL_newstate();
			if (luaL_dofile(L, entry.path().generic_string().c_str()) == LUA_OK) {
				std::filesystem::path fspath = std::filesystem::relative(entry.path(), std::format("mods/{}/credits", modid)).generic_string();
				std::string path = std::format("A{}/{}.credits", fspath.parent_path().generic_string(), fspath.stem().generic_string());

				Credits credits;
				credits.deserialize(L);

				gModDb.credits[path] = credits;
			}
			else {
				post_build_message(lua_tostring(L, -1));
			}
			lua_pop(L, 1);
			lua_close(L);
		}
	}

	if (std::filesystem::exists(std::format("mods/{}/textures", modid))) {
		for (auto const& entry : std::filesystem::recursive_directory_iterator(std::format("mods/{}/textures", modid))) {
			if (entry.is_directory()) continue;
			if (entry.path().extension().generic_string() != ".dds") continue;

			post_build_message(entry.path().generic_string());

			auto engineRelative = std::filesystem::relative(entry.path(), std::format("mods/{}/textures/engine", modid));
			bool isEngineRelativePath = !engineRelative.empty() && !engineRelative.string().rfind("..", 0) == 0;

			auto relative = std::filesystem::relative(entry.path(), std::format("mods/{}/textures{}", modid, isEngineRelativePath ? "/engine" : ""));

			std::string infostem = relative.stem().generic_string();
			auto it = infostem.find_last_of('+');
			std::string stem = infostem.substr(0, it);
			std::uint32_t extra = std::stoul(infostem.substr(it + 1), nullptr, 16);

			std::string hashinput = std::format("{}{}/{}.png", isEngineRelativePath ? "E" : "A", relative.parent_path().generic_string(), stem);
			hashinput.resize(hashinput.size() + 4);
			memcpy(hashinput.data() + hashinput.size() - 4, &extra, 4);

			gModDb.textures[hashinput] = entry.path().generic_string();


		}
	}
}

#include <unordered_map>

void find_mods() {
	// load initial mod state
	lua_State* L = luaL_newstate();
	if (luaL_dofile(L, "aurora/config.lua") == LUA_OK) {
		lua_pushnil(L);
		while (lua_next(L, -2)) {
			lua_getfield(L, -1, "modid");
			std::string modid = lua_tostring(L, -1);
			lua_pop(L, 1);

			lua_getfield(L, -1, "enabled");
			bool enabled = lua_toboolean(L, -1);
			lua_pop(L, 1);

			lua_pop(L, 1);

			gFoundMods.emplace_back(modid, enabled);
		}
	}
	lua_close(L);

	// check if mods exist, if not remove them from the list

	for (auto it = gFoundMods.begin(); it != gFoundMods.end();) {
		if (!std::filesystem::exists(std::filesystem::path("mods") / it->modid)) {
			it = gFoundMods.erase(it);
		}
		else {
			++it;
		}
	}

	// read mods directory, if any mods here arent loaded then add them to the list
	std::unordered_set<std::string> list;
	for (auto const& modentry : gFoundMods) { list.insert(modentry.modid); }

	for (const auto& entry : std::filesystem::directory_iterator("mods")) {
		if (!entry.is_directory()) continue;

		std::string modid = entry.path().filename().generic_string();

		if (!list.contains(modid)) {
			gFoundMods.emplace_back(modid, false);
		}
	}
}

void save_mod_order_state() {
	lua_State* L = luaL_newstate();
	lua_newtable(L);
	
	int index = 1;
	for (auto const& [modid, enabled] : gFoundMods) {
		lua_newtable(L);
		lua_pushstring(L, modid.c_str());
		lua_setfield(L, -2, "modid");
		lua_pushboolean(L, enabled);
		lua_setfield(L, -2, "enabled");

		lua_rawseti(L, -2, index++);
	}

	std::string modloadstate = std::string("return ") + aurora::lapi_serialize(L);

	std::ofstream stream("aurora/config.lua", std::ios::binary);
	stream << modloadstate;
	stream.close();
}

void build() {
	// Save mod order and enable flags
	save_mod_order_state();

	if (!std::filesystem::exists("mods/base")) {
		post_build_message("Thumper content has not been unpacked");
		post_build_message("Aurora cannot build mod content until this is done");
		return;
	}

	for (auto const& [modid, enabled] : gFoundMods) {
		if (!enabled) continue;
		load_mod(modid);
	}

	for (auto const& [modid, enabled] : gFoundMods) {
		if (!enabled) continue;
		process_mod_hooks(modid);
	}

	post_build_message("Building assets");

	for (auto const& [key, table] : gModDb.localization) {
		post_build_message("`{}`", key);
		std::vector<LocalizationEntry> enteries;

		std::uint32_t totalBytes = 0;

		for (auto& [key, value] : table) {
			LocalizationEntry entry;
			entry.key = static_cast<uint32_t>(key);
			entry.offset = totalBytes;
			entry.value = value;

			totalBytes += value.size() + 1;

			enteries.emplace_back(std::move(entry));
		}

		ByteStream stream;
		stream.write_u32(16);
		
		stream.write_u32(enteries.size());
		stream.write_u32(totalBytes);

		for (auto const& entry : enteries) {
			stream.write_cstr(entry.value);
		}

		for (auto const& entry : enteries) {
			stream.write_u32(static_cast<std::uint32_t>(entry.key));
			stream.write_u32(entry.offset);
		}

		write_to_thumper_cache(aurora::fnv1a(key), stream.mBuffer);
	}

	for (auto const& [key, table] : gModDb.credits) {
		post_build_message("`{}`", key);

		ByteStream stream;
		stream.write_u32(16);
		table.serialize(stream);

		write_to_thumper_cache(aurora::fnv1a(key), stream.mBuffer);
	}
	int counter = 0;

	try {

		for (auto const& [target, source] : gModDb.textures) {
			//post_build_message("`{}`", source);

			++counter;

			auto bytes = aurora::read_file(source);

			if (bytes) {
				std::uint32_t key = aurora::fnv1a(target);

				ByteStream stream;
				stream.write_u32(14);

				stream.mBuffer.resize(stream.mBuffer.size() + bytes->size());
				memcpy(stream.mBuffer.data() + 4, bytes->data(), bytes->size());

				write_to_thumper_cache(key, stream.mBuffer);
			}


		}
	}
	catch (std::exception const& e) {
		std::string s = e.what();
	}

	post_build_message("Done");
}


ImFont* gVariableSpace = nullptr;
ImFont* gMonoSpace = nullptr;

static bool gShouldLaunchThumper = false;

namespace aurora {
	bool should_launch_thumper() { return gShouldLaunchThumper; }
}

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

std::optional<std::string> mPathImHex;
std::optional<std::string> mPathHxD;

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

	if (ImGui::Begin("Binary Search", &aOpen)) {
		ImGui::SetNextItemShortcut(ImGuiMod_Ctrl | ImGuiKey_F);
		if (ImGui::InputText("Input", &input, ImGuiInputTextFlags_EnterReturnsTrue)) {
			if (!input.empty() && !future) {
				future = std::async(std::launch::async, [](std::string input) {
					Result result;
					
					std::string parsed = aurora::unescape(input);
					auto parsedSpan = std::as_bytes(std::span(parsed));
					result.pattern = input;

					for (auto const& entry : std::filesystem::directory_iterator("cache")) {
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

					if(std::filesystem::exists("THUMPER_win8.exe.unpacked.exe")){
						auto const data = aurora::read_file("THUMPER_win8.exe.unpacked.exe");

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
			ImGui::ProgressBar(-1.0f * (float)ImGui::GetTime());
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
				}

				if (ImGui::BeginPopupContextItem()) {
					if (mPathHxD) {
						if (ImGui::MenuItem("Open File in HxD")) {
							spawn_process_with_path_argument(*mPathHxD, fmt::format("cache/{}", match.file));
						}
					}

					if (mPathImHex) {
						if (ImGui::MenuItem("Open File in ImHex")) {
							spawn_process_with_path_argument(*mPathImHex, fmt::format("cache/{}", match.file));
						}
					}

					ImGui::EndPopup();
				}

				
				ImGui::SetItemTooltip("Origin File: %s", match.file.c_str());
			}

			if (copy) ImGui::LogFinish();
		}
	}
	ImGui::End();
}

int lua_print(lua_State* L) {
	console.log(lua_tostring(L, 1));
	return 0;
}

void cache_scan(std::unordered_set<std::string>& pcFileStorage) {
	console.log("Scanning cache...");

	pcFileStorage.clear();

	for (auto const& entry : std::filesystem::directory_iterator("cache")) {
		pcFileStorage.insert(entry.path().filename().generic_string());
	}

	console.log(fmt::format("{} file(s)", pcFileStorage.size()));

}

std::unordered_set<std::string> pcFileStorage;

int aurora_rhash(lua_State* L) {
	auto it = gHashtable.find(static_cast<std::uint32_t>(luaL_checkinteger(L, 1)));
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
		lua_pushliteral(L, ""); // Backward compat, will be deprecated and removed
		return 1;
	});
	lua_setfield(L, -2, "game_directory");

	lua_pop(L, 1);

	return L;
}

void restore_cache_content() {
	for (auto& entry : std::filesystem::directory_iterator("cache")) {
		std::string backupFile = std::format("{}.bak", entry.path().generic_string());
		if (std::filesystem::exists(backupFile)) {
			std::filesystem::copy_file(backupFile, entry.path(), std::filesystem::copy_options::overwrite_existing);
		}
	}
}

struct PluginEngine {
	struct Plugin {
		bool visible = false;
		bool wantsFocus = false;
	};

	std::unordered_map<std::string, Plugin> plugins;

	void reload() {
		shutdown();

		L = aurora_newstate();

		if (luaL_dofile(L, "aurora/boot.lua") != LUA_OK) {
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

				gHashtable[static_cast<std::uint32_t>(lua_tointeger(L, -1))] = std::string(data, size);

				lua_pop(L, 2);
			}
		}
		lua_pop(L, 1);

		// Plugins are loaded, check the visible flag
		if (lua_getfield(L, -1, "plugins") == LUA_TTABLE) {
			lua_pushnil(L);
			while (lua_next(L, -2)) {
				lua_pushvalue(L, -2);
				char const *key = lua_tostring(L, -1);

				if (!plugins.contains(key)) {
					Plugin& storage = plugins[key];

					if (lua_getfield(L, -2, "visible") == LUA_TBOOLEAN) {
						storage.visible = lua_toboolean(L, -1);
					}
					lua_pop(L, 1);
				}

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

						// Get the storage container for the plugin
						// Allocate it if needed
						Plugin& storage = plugins[key];

						lua_getfield(L, -2, "enabled");
						bool const enabled = lua_toboolean(L, -1);
						lua_pop(L, 1);

						if (enabled) {
							if (lua_getfield(L, -2, "gui") == LUA_TTABLE) {
								ImGui::MenuItem(key, nullptr, &storage.visible);
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

		// Check messages

		if (lua_getfield(L, 1, "plugins") == LUA_TTABLE) {
			if (lua_getglobal(L, "_AuroraImplGlobalStorage") == LUA_TTABLE) {
				lua_pushnil(L);
				while (lua_next(L, -2)) {

					lua_getfield(L, -1, "target");
					char const* targetKey = lua_tostring(L, -1);
					lua_gettable(L, -5);

					if (lua_getfield(L, -1, "OnMessageRecieved") == LUA_TFUNCTION) {
						lua_getfield(L, -3, "source");
						lua_getfield(L, -4, "action");
						lua_getfield(L, -5, "data");
						lua_pcall(L, 3, 0, 0);

						plugins[targetKey].wantsFocus = true;
					}
					else lua_pop(L, 1);

					lua_pop(L, 2);
				}

				lua_pushnil(L);
				lua_setglobal(L, "_AuroraImplGlobalStorage");
			}
			lua_pop(L, 1);
		}
		lua_pop(L, 1);

		// iterate plugins
		if (lua_getfield(L, -1, "plugins") == LUA_TTABLE) {
			lua_pushnil(L);
			while (lua_next(L, -2)) {
				lua_pushvalue(L, -2);
				char const *key = lua_tostring(L, -1);

				// Update impl global, used for plugin messages
				lua_pushstring(L, key);
				lua_setglobal(L, "_AuroraImplCurrentPlugin");

				// Get the storage container for the plugin
				// Allocate it if needed
				Plugin& storage = plugins[key];

				lua_getfield(L, -2, "enabled");
				bool const enabled = lua_toboolean(L, -1);
				lua_pop(L, 1);

				if (enabled) {
					// If a gui is defined for this plugin
					if (lua_getfield(L, -2, "gui") == LUA_TTABLE) {
						lua_getfield(L, -1, "title");
						char const* title = lua_tostring(L, -1);
						lua_pop(L, 1);

						std::string const debugTitle = fmt::format("{} ({})", title, key);

						if (storage.wantsFocus) {
							storage.visible = true;
							storage.wantsFocus = false;
							ImGui::SetNextWindowFocus();
						}

						if (storage.visible) {
							ImGui::SetNextWindowSize(ImVec2(640, 480), ImGuiCond_FirstUseEver);
							if (ImGui::Begin(debugTitle.c_str(), &storage.visible)) {
								if (lua_getfield(L, -1, "OnGui") == LUA_TFUNCTION) {
									if (lua_pcall(L, 0, 0, 0) != LUA_OK) {
										spdlog::error(lua_tostring(L, -1));
										ImGui::TextUnformatted(lua_tostring(L, -1));
										lua_pop(L, 1);
									}
								}
								else lua_pop(L, 1);
							}
							ImGui::End();
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
						if (lua_pcall(L, 0, 0, 0) != LUA_OK) {
							spdlog::error(lua_tostring(L, -1));
							lua_pop(L, 1); // Pop error value from stack
						}
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
	spdlog::critical(message);
	tinyfd_messageBox("Critical Error", message.c_str(), "ok", "error", 1);
	throw std::runtime_error(message);
}

void create_window_icons(GLFWwindow* window) {
	using stbir_deleter = aurora::DeleterOf < [](void* p) {STBIR_FREE(p, 0); } > ;
	using stbi_deleter = aurora::DeleterOf<stbi_image_free>;

	int x, y;
	std::unique_ptr<stbi_uc, stbi_deleter> pixels = decltype(pixels)(stbi_load("aurora/icon.png", &x, &y, nullptr, 4));

	if (pixels) {
		std::array<std::unique_ptr<unsigned char, stbir_deleter>, 4> imageMemory;
		std::array<GLFWimage, imageMemory.size()> glfwImages;

		for (int i = 0; i < imageMemory.size(); ++i) {
			int const size = (i + 1) * 16;
			imageMemory[i] = decltype(imageMemory)::value_type(stbir_resize_uint8_srgb(pixels.get(), x, y, 0, nullptr, size, size, 0, STBIR_RGBA));
			glfwImages[i].width = size;
			glfwImages[i].height = size;
			glfwImages[i].pixels = imageMemory[i].get();
		}

		glfwSetWindowIcon(window, static_cast<int>(glfwImages.size()), glfwImages.data());
	}
}


namespace aurora {
void main() {
	mPathImHex = std::format("{}/ImHex/imhex-gui.exe", get_program_files_directory());
	mPathHxD = std::format("{}/HxD/HxD.exe", get_program_files_directory());

	if (!std::filesystem::exists(*mPathImHex)) mPathImHex = std::nullopt;
	if (!std::filesystem::exists(*mPathHxD)) mPathHxD = std::nullopt;

	bool toolsBinarySearch = false;
	bool open = true;
	
	cache_scan(pcFileStorage);

	glfwSetErrorCallback([](int errorCode, const char *description) {
		spdlog::error(description);
	});

	if (!glfwInit()) throw_error_box("Failed to initialize GLFW");

	auto const* vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
	constexpr int kWidth = 1280;
	constexpr int kHeight = 720;
	glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
	glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 5);
	glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
	glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
	glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
	glfwWindowHint(GLFW_POSITION_X, vidmode->width / 2 - kWidth / 2);
	glfwWindowHint(GLFW_POSITION_Y, vidmode->height / 2 - kHeight / 2);

	GLFWwindow* window = glfwCreateWindow(1280, 720, "Aurora v0.0.4-a.7", nullptr, nullptr);
	if (!window) throw_error_box("Failed to create GLFW window");

	create_window_icons(window);

	glfwMakeContextCurrent(window);
	if (!gladLoadGL(&glfwGetProcAddress)) throw_error_box("Failed to initialize GLAD");

	PluginEngine pluginEngine;
	pluginEngine.reload();

	IMGUI_CHECKVERSION();
	ImGui::CreateContext();
	ImGuiIO& io = ImGui::GetIO();
	io.ConfigFlags |= ImGuiConfigFlags_NavEnableKeyboard;
	io.ConfigFlags |= ImGuiConfigFlags_DockingEnable;

	gVariableSpace = io.Fonts->AddFontFromFileTTF("aurora/NotoSans-Regular.ttf", 18.0f);
	gMonoSpace = io.Fonts->AddFontFromFileTTF("aurora/NotoSansMono-Regular.ttf", 18.0f);

	ImGui::StyleColorsDark();
	ImGui_ImplGlfw_InitForOpenGL(window, true);
	ImGui_ImplOpenGL3_Init("#version 450 core");

	console.log("Welcome to Aurora!");

	bool showDemo = false;

	aurora::GuiHasher hasher;
	bool hasherVisible = false;

	find_mods();

	while(!glfwWindowShouldClose(window)) {
		glfwPollEvents();

		ImGui_ImplOpenGL3_NewFrame();
		ImGui_ImplGlfw_NewFrame();
		ImGui::NewFrame();

		glEnable(GL_DEPTH_TEST);

		ImGui::DockSpaceOverViewport();

		if (ImGui::BeginMainMenuBar()) {
			if (ImGui::BeginMenu("View")) {
				ImGui::MenuItem("Console", nullptr, &open);
				ImGui::MenuItem("Hasher", nullptr, &hasherVisible);
				ImGui::Separator();
				ImGui::MenuItem("Dear ImGui Demo", ImGui::GetKeyChordName(ImGuiMod_Ctrl | ImGuiMod_Shift | ImGuiKey_D), &showDemo);
				ImGui::EndMenu();
			}

			if (ImGui::BeginMenu("Tools")) {
				
				ImGui::MenuItem("Binary Search", nullptr, &toolsBinarySearch);
				
				if (ImGui::MenuItem("Restore Cache Backup")) {
					restore_cache_content();
				}

				ImGui::EndMenu();
			}
		}

		ImGui::EndMainMenuBar();

		bool v = true;
		unpack_gui(v);

		static std::optional<std::future<void>> buildFuture = std::nullopt;

		if (!buildFuture.has_value()) {
			ImGui::SetNextWindowPos(ImVec2(io.DisplaySize.x * 0.5f, io.DisplaySize.y * 0.5f), ImGuiCond_Once, ImVec2(0.5f, 0.5f));
			ImGui::SetNextWindowSize(ImVec2(800, 600), ImGuiCond_Once);
			if (ImGui::Begin("Launcher")) {
				static bool buildModContent = true;


				ImGui::Checkbox("Build Mod Content", &buildModContent);



				if (ImGui::Button("Launch Thumper")) {
					gShouldLaunchThumper = true;

					if (buildModContent) {
						buildFuture = std::async(std::launch::async, build);
					}
					else {
						glfwSetWindowShouldClose(window, true);
					}
				}

				ImGui::SeparatorText("Mod Load Order");

				ImGui::PushItemFlag(ImGuiItemFlags_AllowDuplicateId, true);

				for (std::size_t n = 0; n < gFoundMods.size(); n++) {
					auto const& item = gFoundMods[n];


					ImGui::PushID(n);
					
					

					

					if (ImGui::Button("Up")) {
						if (n > 0) std::swap(gFoundMods[n], gFoundMods[n - 1]);
					}

					ImGui::SameLine();

					if (ImGui::Button("Down")) {
						if(n < gFoundMods.size() - 1) std::swap(gFoundMods[n], gFoundMods[n + 1]);
					}

					ImGui::SameLine();

					ImGui::Checkbox("###Enabled", &gFoundMods[n].enabled);
					ImGui::SameLine();

					ImGui::Selectable(item.modid.c_str());

					ImGui::PopID();
				}

				ImGui::PopItemFlag();
			}
			ImGui::End();
		}
		else {
			ImGui::SetNextWindowPos(ImVec2(io.DisplaySize.x * 0.5f, io.DisplaySize.y * 0.5f), ImGuiCond_Once, ImVec2(0.5f, 0.5f));
			ImGui::SetNextWindowSize(ImVec2(800, 600), ImGuiCond_Once);
			if (ImGui::Begin("Building")) {
				const float footer_height_to_reserve = ImGui::GetStyle().ItemSpacing.y + ImGui::GetFrameHeightWithSpacing();
				if (ImGui::BeginChild("ScrollingRegion", ImVec2(0, -footer_height_to_reserve), ImGuiChildFlags_NavFlattened, ImGuiWindowFlags_HorizontalScrollbar)) {
					ImGui::PushStyleVar(ImGuiStyleVar_ItemSpacing, ImVec2(4, 1));

					std::lock_guard lck{ gBuildMessagesMtx };

					for (auto const& message : gBuildMessages) {
						ImGui::TextUnformatted(message.c_str(), message.c_str() + message.size());
					}

					bool ScrollToBottom = false;
					bool AutoScroll = true;

					if (ScrollToBottom || (AutoScroll && ImGui::GetScrollY() >= ImGui::GetScrollMaxY()))
						ImGui::SetScrollHereY(1.0f);

					ImGui::PopStyleVar();
				}
				ImGui::EndChild();

				if (aurora::is_future_ready(buildFuture.value())) {
					

					if (ImGui::Button("Launch")) {
						glfwSetWindowShouldClose(window, true);
						gShouldLaunchThumper = true;
					}

					
				}
			}
			ImGui::End();

		}

		

		hasher.on_gui(hasherVisible);

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
