#ifdef _WIN32
#	include <Windows.h>
#	include <shellapi.h>
#	undef max
#	undef min
#endif

#include <glad/gl.h>
#include <GLFW/glfw3.h>
#define IMGUI_DEFINE_MATH_OPERATORS
#include <imgui.h>
#include <imgui_internal.h>
#include <misc/cpp/imgui_stdlib.h>
#include <backends/imgui_impl_glfw.h>
#include <backends/imgui_impl_opengl3.h>

#include "lua_api.hpp"

#include <lua.hpp>
#include <tinyfiledialogs.h>

#include <spdlog/spdlog.h>
#include <spdlog/sinks/basic_file_sink.h>

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

#include <minizip/unzip.h>

#define STB_IMAGE_IMPLEMENTATION
#include "stb_image.h"


#include "au_window.hpp"

#include "au_util.hpp"

#include "au_hash.hpp"
#include "au_lua_serialize.hpp"
#include "gui/au_hasher.hpp"

#include "au_serialize.hpp"


std::unordered_map<std::uint32_t, std::string> gHashtable;

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

struct AuroraParseError : public std::runtime_error {
	explicit AuroraParseError(std::string const& message) : std::runtime_error(message.c_str()) {}
	explicit AuroraParseError(char const* message) : std::runtime_error(message) {}
};


std::string hashedStringGui(std::uint32_t hash) {
	auto it = gHashtable.find(hash);

	if (it != gHashtable.end()) return std::string(it->second);

	return std::format("[{:x}]", hash);
}

struct ObjlibParser final {
	struct GlobalImport {
		std::uint32_t unknown;
		std::string path;

		void deserialize(aurora::ByteStream& s) {
			unknown = s.read_u32();
			path = s.read_sstr();
		}
	};

	struct ObjectImport {
		std::uint32_t type;
		std::string name;
		std::uint32_t unknown;
		std::string path;

		void deserialize(aurora::ByteStream& s) {
			type = s.read_u32();
			name = s.read_sstr();
			unknown = s.read_u32();
			path = s.read_sstr();
		}
	};

	struct ObjectDeclaration {
		std::uint32_t type;
		std::string name;

		void deserialize(aurora::ByteStream& s) {
			type = s.read_u32();
			name = s.read_sstr();
		}
	};

	struct SequinMasterLvl final {
		std::string lvlName;
		std::string gateName;
		bool isCheckpoint;
		std::string checkpointLeaderLvlName;
		std::string restLvlName;

		bool unknownBool0;
		bool unknownBool1;
		uint32_t unknown0;
		bool unknownBool2;

		bool playPlus;

		void deserialize(aurora::ByteStream& aStream) {
			lvlName = aStream.read_sstr();
			gateName = aStream.read_sstr();
			isCheckpoint = aStream.read_u8();
			checkpointLeaderLvlName = aStream.read_sstr();
			restLvlName = aStream.read_sstr();

			unknownBool0 = aStream.read_u8();
			unknownBool1 = aStream.read_u8();
			unknown0 = aStream.read_u32();
			unknownBool2 = aStream.read_u8();

			playPlus = aStream.read_u8();
		}
	};

	struct SequinMaster final {
		static constexpr std::array<std::uint32_t, 4> kHeader = { 33, 33, 4, 2 };

		std::array<uint32_t, 4> header = kHeader;
		uint32_t hash0;
		uint32_t unknown0 = 1;
		uint32_t hash1;
		std::string timeUnit = "kTimeBeats";
		uint32_t hash2;		//editstatecomp
		uint32_t unknown1 = 0;
		float unknown2;
		std::string skybox;
		std::string introLvl;

		std::vector<SequinMasterLvl> sublevels;

		uint8_t footer1;
		uint8_t footer2;
		uint32_t footer3;
		uint32_t footer4;
		uint32_t footer5;
		uint32_t footer6;
		float footer7;
		float footer8;
		float footer9;
		std::string checkpointLvl;
		std::string pathGameplay = "path.gameplay";

		void deserialize(aurora::ByteStream& aStream) {
			for (size_t i = 0; i < kHeader.size(); ++i) {
				assert(header[i] = aStream.read_u32() == kHeader[i]);
			}

			hash0 = aStream.read_u32();
			unknown0 = aStream.read_u32();
			hash1 = aStream.read_u32();
			timeUnit = aStream.read_sstr();
			hash2 = aStream.read_u32();
			unknown1 = aStream.read_u32();
			unknown2 = aStream.read_f32();
			skybox = aStream.read_sstr();
			introLvl = aStream.read_sstr();

			sublevels.resize(aStream.read_u32());

			for (auto& sublevel : sublevels) {
				sublevel.deserialize(aStream);
			}

			footer1 = aStream.read_u8();
			footer2 = aStream.read_u8();
			footer3 = aStream.read_u32();
			footer4 = aStream.read_u32();
			footer5 = aStream.read_u32();
			footer6 = aStream.read_u32();
			footer7 = aStream.read_f32();
			footer8 = aStream.read_f32();
			footer9 = aStream.read_f32();
			checkpointLvl = aStream.read_sstr();
			pathGameplay = aStream.read_sstr();
		}
	};

	void parse(std::string_view aPath) {
		std::vector<std::byte> buffer;

		if (auto tempBuffer = aurora::read_file(std::format("cache/{}", aPath))) { buffer = tempBuffer.value(); }
		else if (cache_file_exists(aurora::fnv1a(aPath))) { buffer = *aurora::read_file(std::format("cache/{:x}.pc", aurora::fnv1a(aPath))); }

		if (buffer.size() == 0) throw AuroraParseError("Failed to parse file, no bytes returned");

		aurora::ByteStream s;
		s.mBuffer = std::move(buffer);

		mFileType = s.read_u32();
		mObjlibType = s.read_u32();

		// Ignore, sometimes 3 values, sometimes 4
		s.read_u32();
		s.read_u32();
		s.read_u32();
		s.read_u32();

		mGlobalImports.resize(s.read_u32());
		for (auto& element : mGlobalImports) element.deserialize(s);

		mPath = s.read_sstr();

		mObjectImports.resize(s.read_u32());
		for (auto& element : mObjectImports) element.deserialize(s);

		mObjectDeclarations.resize(s.read_u32());
		for (auto& element : mObjectDeclarations) element.deserialize(s);

		// object readback

		for (auto& declaration : mObjectDeclarations) {
			if (declaration.type == aurora::fnv1a("SequinMaster")) {
				std::span<std::byte const> headerBytes = std::as_bytes(std::span(SequinMaster::kHeader));

				auto it = std::search(s.mBuffer.begin() + s.mOffset, s.mBuffer.end(), headerBytes.begin(), headerBytes.end());

				if (it != s.mBuffer.end()) {
					s.mOffset += std::distance(s.mBuffer.begin() + s.mOffset, it);

					SequinMaster master;
					master.deserialize(s);
					mMasters[declaration.name] = std::move(master);

				}
			}
		}

		loadedFile = aPath;
	}

	std::string mSelectedObject;
	std::unordered_map<std::string, SequinMaster> mMasters;

	std::string loadedFile;

	void guiEditors() {
		std::string title = std::format("Master: {} ({})###Master", mSelectedObject, loadedFile);
		if (auto it = mMasters.find(mSelectedObject); it != mMasters.end()) {
			if (ImGui::Begin(title.c_str())) {
				auto& ref = it->second;
				ImGui::InputText("Time Unit", &ref.timeUnit);
				ImGui::InputText("Skybox", &ref.skybox);
				ImGui::InputText("Intro", &ref.introLvl);
				ImGui::InputText("Checkpoint", &ref.checkpointLvl);
				ImGui::InputText("Gameplay", &ref.pathGameplay);

				ImGui::InputScalar("hash0", ImGuiDataType_U32, &ref.hash0);
				ImGui::InputScalar("unknown0", ImGuiDataType_U32, &ref.unknown0);
				ImGui::InputScalar("hash1", ImGuiDataType_U32, &ref.hash1);
				ImGui::InputScalar("hash2", ImGuiDataType_U32, &ref.hash2);
				ImGui::InputScalar("unknown1", ImGuiDataType_U32, &ref.unknown1);

				ImGui::InputFloat("Unknown 2", &ref.unknown2);

				ImGui::InputScalar("footer1", ImGuiDataType_U8, &ref.footer1);
				ImGui::InputScalar("footer2", ImGuiDataType_U8, &ref.footer2);

				ImGui::InputScalar("footer3", ImGuiDataType_U32, &ref.footer3);
				ImGui::InputScalar("footer4", ImGuiDataType_U32, &ref.footer4);
				ImGui::InputScalar("footer5", ImGuiDataType_U32, &ref.footer5);
				ImGui::InputScalar("footer6", ImGuiDataType_U32, &ref.footer6);

				ImGui::InputFloat("Footer 7", &ref.footer7);
				ImGui::InputFloat("Footer 8", &ref.footer8);
				ImGui::InputFloat("Footer 9", &ref.footer9);










				ImGui::Separator();



				for (auto it = ref.sublevels.begin(); it != ref.sublevels.end();) {
					bool shouldDelete = false;

					auto& element = *it;

					std::string const& name = element.gateName.empty() ? element.lvlName : element.gateName;
					bool open = ImGui::TreeNodeEx(name.c_str());

					if (ImGui::BeginPopupContextItem()) {
						if (ImGui::MenuItem("Delete")) {
							shouldDelete = true;
						}

						ImGui::EndPopup();
					}

					if (open) {

						ImGui::InputText("Level", &element.lvlName);
						ImGui::InputText("Gate", &element.gateName);
						ImGui::Checkbox("Has Checkpoint", &element.isCheckpoint);
						ImGui::InputText("Leader Level", &element.checkpointLeaderLvlName);
						ImGui::InputText("Rest Level", &element.restLvlName);
						ImGui::Checkbox("Unknown Bool 0", &element.unknownBool0);
						ImGui::Checkbox("Unknown Bool 1", &element.unknownBool1);
						ImGui::InputScalar("Unknown 0", ImGuiDataType_U32, &element.unknown0);
						ImGui::Checkbox("Unknown Bool 2", &element.unknownBool2);
						ImGui::Checkbox("Play Plus", &element.playPlus);

						ImGui::TreePop();
					}

					if (shouldDelete) {
						it = ref.sublevels.erase(it);
					}
					else {
						++it;
					}
				}
			}
			ImGui::End();
		}

			
	}

	void gui() {

		ImGui::InputText("Selected File", &mSelectedObject);

		ImGui::Separator();

		ImGui::LabelText("File Type", "%d", mFileType);
		ImGui::LabelText("Objlib Type", "%x", mObjlibType); // LevelLib

		if (ImGui::CollapsingHeader("Global Imports")) {
			ImGui::PushID("Global Imports");
			for (std::size_t i = 0; i < mGlobalImports.size(); ++i) {
				if (ImGui::TreeNode(mGlobalImports[i].path.c_str())) {

					ImGui::LabelText("Unknown", "%d", mGlobalImports[i].unknown);

					ImGui::TreePop();
				}
			}
			ImGui::PopID();
		}

		ImGui::LabelText("Path", "%s", mPath.c_str());

		if (ImGui::CollapsingHeader("Object Imports")) {
			ImGui::PushID("Object Imports");
			for (std::size_t i = 0; i < mObjectImports.size(); ++i) {
				if (ImGui::TreeNode(mObjectImports[i].name.c_str())) {
					ImGui::LabelText("Type", "%s", hashedStringGui(mObjectImports[i].type).c_str());
					ImGui::LabelText("Unknown", "%d", mObjectImports[i].unknown);
					ImGui::LabelText("Path", "%s", mObjectImports[i].path.c_str());

					ImGui::TreePop();
				}
			}
			ImGui::PopID();
		}

		if (ImGui::CollapsingHeader("Object Declarations")) {
			ImGui::PushID("Object Declarations");

			for (std::size_t i = 0; i < mObjectDeclarations.size(); ++i) {
				std::string text = std::format("{} ({})", mObjectDeclarations[i].name.c_str(), hashedStringGui(mObjectDeclarations[i].type).c_str());
				if (ImGui::Selectable(text.c_str(), mSelectedObject == mObjectDeclarations[i].name)) {
					mSelectedObject = mObjectDeclarations[i].name;
				}
			}
			ImGui::PopID();
		}

	}

	std::uint32_t mFileType;
	std::uint32_t mObjlibType;
	std::vector<GlobalImport> mGlobalImports;
	std::string mPath;
	std::vector<ObjectImport> mObjectImports;
	std::vector<ObjectDeclaration> mObjectDeclarations;
};

static bool gObjlibParserStaticDataVisible = false;
static ObjlibParser gObjlibParserStaticData;

struct LevelListing {
	struct Entry {
		std::string key;
		std::uint32_t unknown0;
		std::string path;
		std::string unlocks;
		bool defaultLocked;
		bool unknown1;
		bool credits;
		std::uint32_t colorIdx0;
		std::uint32_t colorIdx1;

		void deserialize(aurora::ByteStream& stream) {
			key = stream.read_sstr();
			unknown0 = stream.read_u32();
			path = stream.read_sstr();
			unlocks = stream.read_sstr();
			defaultLocked = stream.read_bool();
			unknown1 = stream.read_bool();
			credits = stream.read_bool();
			colorIdx0 = stream.read_u32();
			colorIdx1 = stream.read_u32();
		}

		void serialize(aurora::ByteStream& stream) {
			stream.write_sstr(key);
			stream.write_u32(unknown0);
			stream.write_sstr(path);
			stream.write_sstr(unlocks);
			stream.write_bool(defaultLocked);
			stream.write_bool(unknown1);
			stream.write_bool(credits);
			stream.write_u32(colorIdx0);
			stream.write_u32(colorIdx1);
		}

		void deserialize(lua_State* L) {
			lua_getfield(L, -1, "key"); key = lua_tostring(L, -1); lua_pop(L, 1);
			lua_getfield(L, -1, "unknown0"); unknown0 = static_cast<std::uint32_t>(lua_tointeger(L, -1)); lua_pop(L, 1);
			lua_getfield(L, -1, "path"); path = lua_tostring(L, -1); lua_pop(L, 1);
			lua_getfield(L, -1, "unlocks"); unlocks = lua_tostring(L, -1); lua_pop(L, 1);
			lua_getfield(L, -1, "defaultLocked"); defaultLocked = lua_toboolean(L, -1); lua_pop(L, 1);
			lua_getfield(L, -1, "unknown1"); unknown1 = lua_toboolean(L, -1); lua_pop(L, 1);
			lua_getfield(L, -1, "credits"); credits = lua_toboolean(L, -1); lua_pop(L, 1);
			lua_getfield(L, -1, "colorIdx0"); colorIdx0 = static_cast<std::uint32_t>(lua_tointeger(L, -1)); lua_pop(L, 1);
			lua_getfield(L, -1, "colorIdx1"); colorIdx1 = static_cast<std::uint32_t>(lua_tointeger(L, -1)); lua_pop(L, 1);
		}

		void serialize(lua_State* L) {
			lua_newtable(L);
			lua_pushstring(L, key.c_str()); lua_setfield(L, -2, "key");
			lua_pushinteger(L, unknown0); lua_setfield(L, -2, "unknown0");
			lua_pushstring(L, path.c_str()); lua_setfield(L, -2, "path");
			lua_pushstring(L, unlocks.c_str()); lua_setfield(L, -2, "unlocks");
			lua_pushboolean(L, defaultLocked); lua_setfield(L, -2, "defaultLocked");
			lua_pushboolean(L, unknown1); lua_setfield(L, -2, "unknown1");
			lua_pushboolean(L, credits); lua_setfield(L, -2, "credits");
			lua_pushinteger(L, colorIdx0); lua_setfield(L, -2, "colorIdx0");
			lua_pushinteger(L, colorIdx1); lua_setfield(L, -2, "colorIdx1");
		}
	};

	std::vector<Entry> entries;

	void serialize(aurora::ByteStream& stream) {
		stream.write_u32(static_cast<std::uint32_t>(entries.size()));
		
		for (auto& entry : entries) {
			entry.serialize(stream);
		}
	}

	void deserialize(lua_State* L) {
		entries.clear();

		lua_pushnil(L);
		while(lua_next(L, -2)) {
			Entry e;
			e.deserialize(L);
			entries.emplace_back(std::move(e));
			lua_pop(L, 1);
		}
	}

	void deserialize(aurora::ByteStream& stream) {
		entries.resize(stream.read_u32());

		for (auto& entry : entries) {
			entry.deserialize(stream);
		}
	}

	void serialize(lua_State* L) {
		lua_newtable(L);
		for (int i = 0; i < entries.size(); ++i) {
			entries[i].serialize(L);
			lua_rawseti(L, -2, i + 1);
		}
	}
};

#include "au_serialize.hpp"

struct Credits {
	struct MajorGroupElement {
		std::string decoration;
		std::string text;

		void deserialize(aurora::ByteStream& stream) {
			decoration = stream.read_sstr();
			text = stream.read_sstr();
		}

		void serialize(aurora::ByteStream& stream) const {
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

		void deserialize(aurora::ByteStream& stream) {
			elements.resize(stream.read_u32());

			for (auto& element : elements) {
				element.deserialize(stream);
			}
		}

		void serialize(aurora::ByteStream& stream) const {
			stream.write_u32(static_cast<std::uint32_t>(elements.size()));

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

		void deserialize(aurora::ByteStream& stream) {
			banner = stream.read_sstr();
			thanks.resize(stream.read_u32());

			for (auto& str : thanks) {
				str = stream.read_sstr();
			}
		}

		void serialize(aurora::ByteStream& stream) const {
			stream.write_sstr(banner);
			stream.write_u32(static_cast<std::uint32_t>(thanks.size()));

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

	void deserialize(aurora::ByteStream& stream) {
		major.resize(stream.read_u32());

		for (auto& group : major) {
			group.deserialize(stream);
		}

		minor.resize(stream.read_u32());

		for (auto& group : minor) {
			group.deserialize(stream);
		}
	}

	void serialize(aurora::ByteStream& stream) const {
		stream.write_u32(static_cast<std::uint32_t>(major.size()));

		for (auto& group : major) {
			group.serialize(stream);
		}

		stream.write_u32(static_cast<std::uint32_t>(minor.size()));

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

	void deserialize(aurora::ByteStream& stream) {
		auto cstrCount = stream.read_u32();
		auto byteCount = stream.read_u32();

		enteries.resize(cstrCount);

		for (unsigned int i = 0; i < cstrCount; ++i) {
			enteries[i].value = stream.read_cstr();
		}

		for (unsigned int i = 0; i < cstrCount; ++i) {
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

void unpack_levels() {
	if (auto bytes = aurora::read_file(std::format("cache/{:x}.pc", aurora::fnv1a("Aui/thumper.levels")))) {
		aurora::ByteStream stream;
		stream.mBuffer = std::move(*bytes);

		lua_State* L = luaL_newstate();
		luaL_openlibs(L);

		stream.read_u32(); // ignore header // 16
		LevelListing locs;
		locs.deserialize(stream);
		locs.serialize(L);

		std::string readyToWrite = std::string("return ") + aurora::lapi_serialize(L);

		std::string writePath = "mods/base/levels/ui/thumper.lua";
		std::filesystem::create_directories(std::filesystem::path(writePath).parent_path());

		std::ofstream s(writePath, std::ios::binary);
		s.write(readyToWrite.data(), readyToWrite.size());
		s.close();

		lua_close(L);
	}
}


void unpack_localization() {
	lua_State* L = luaL_newstate();

	if (luaL_dofile(L, "aurora/localization.lua") == LUA_OK) {

		lua_pushnil(L);
		while (lua_next(L, -2)) {
			std::size_t size;
			char const* data = lua_tolstring(L, -1, &size);
			auto hash = aurora::fnv1a(data, size);

			if (auto bytes = aurora::read_file(std::format("cache/{:x}.pc", hash))) {
				aurora::ByteStream stream;
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
				aurora::ByteStream stream;
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
				aurora::ByteStream stream;
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
	if (ImGui::Begin("Unpacker", &visible)) {
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

		if (ImGui::Button("Unpack Levels (level listing)")) {
			unpack_levels();
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

struct ModDb {
	std::unordered_map<std::string, std::unordered_map<LocalizationKey, std::string>> localization;
	std::unordered_map<std::string, Credits> credits;

	std::unordered_map<std::string, std::string> textures; // maps the texture name to the texture target

	std::unordered_map<std::string, LevelListing> listings;
};

ModDb gModDb;

struct ModEntry {
	std::string modid;
	bool enabled;
};

static std::vector<ModEntry> gFoundMods;


void process_mod_hooks(std::string const& modid) {
	spdlog::info("Processing patches `{}`", modid);

	if (std::filesystem::exists(std::format("mods/{}/patches/levels", modid))) {
		for (auto const& entry : std::filesystem::recursive_directory_iterator(std::format("mods/{}/patches/levels", modid))) {
			if (entry.is_directory()) continue;
			if (entry.path().extension().generic_string() != ".lua") continue;

			spdlog::info(entry.path().generic_string());

			lua_State* L = luaL_newstate();
			luaL_openlibs(L);

			std::filesystem::path fspath = std::filesystem::relative(entry.path(), std::format("mods/{}/patches/levels", modid)).generic_string();
			std::string path = std::format("A{}/{}.levels", fspath.parent_path().generic_string(), fspath.stem().generic_string());

			gModDb.listings[path].serialize(L);

			if (luaL_dofile(L, entry.path().generic_string().c_str()) == LUA_OK) {
				lua_pushvalue(L, -2);
				lua_pcall(L, 1, 0, 0);
				gModDb.listings[path].deserialize(L);
			}
			else {
				spdlog::error("Lua Error {}:", lua_tostring(L, -1));
			}
			lua_pop(L, 1);
			lua_close(L);
		}
	}

	if (std::filesystem::exists(std::format("mods/{}/patches/credits", modid))) {
		for (auto const& entry : std::filesystem::recursive_directory_iterator(std::format("mods/{}/patches/credits", modid))) {
			if (entry.is_directory()) continue;
			if (entry.path().extension().generic_string() != ".lua") continue;

			spdlog::info(entry.path().generic_string());

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
				spdlog::error("Lua Error: {}", lua_tostring(L, -1));
			}
			lua_pop(L, 1);
			lua_close(L);
		}
	}
}


struct TCLEPrecompiledLevel {
	std::string name;
	std::string localizationKey;
	std::string description;
	std::string difficulty;
	std::string authors;

	std::unordered_map<std::string, std::vector<std::byte>> files;
};

void load_precompiled_tcle_mods(std::string const& modid) {
	spdlog::info("Loading TCLE Precompiled `{}`", modid);

	std::string path = std::format("mods/{}.zip", modid);

	unzFile file = unzOpen(path.c_str());
	if (!file) return;

	unz_global_info global_info;
	if (unzGetGlobalInfo(file, &global_info) != UNZ_OK) {
		unzClose(file);
		return;
	}

	TCLEPrecompiledLevel level;

	for (decltype(global_info.number_entry) i = 0; i < global_info.number_entry; ++i) {
		unz_file_info file_info;

		std::array<char, 256> filename;

		if (unzGetCurrentFileInfo(file, &file_info, filename.data(), static_cast<uLong>(filename.size()), nullptr, 0, nullptr, 0) != UNZ_OK) {
			unzClose(file);
			continue;
		}

		if (unzOpenCurrentFile(file) == UNZ_OK) {
			std::vector<std::byte> buffer;
			buffer.resize(file_info.uncompressed_size);

			int retValue = unzReadCurrentFile(file, buffer.data(), static_cast<unsigned int>(buffer.size()));
			assert(retValue == file_info.uncompressed_size);

			level.files[std::string(filename.data())] = std::move(buffer);
		}

		unzCloseCurrentFile(file);
		unzGoToNextFile(file);
	}

	unzClose(file);

	// Level content is loaded into memory, start scanning file contents to figure out where to store data

	// Step 1. scan the .tcl file and find out some stuff
	for (auto const& [key, value] : level.files) {
		if (!key.ends_with(".TCL")) continue;

		spdlog::info("Found .tcl: {}", key);
		break;
	}

	std::string origin;

	for (auto& [key, value] : level.files) {
		if (!key.ends_with(".objlib")) continue;

		// move buffer into stream for reading, no need to restore the content afterwards since this is the only step to touch the .objlib
		aurora::ByteStream stream;
		stream.mBuffer = std::move(value);

		for(int i = 0; i < 6; ++i)
			stream.read_u32(); // skip file header and unknowns fields
		

		auto importCount = stream.read_u32();

		for (unsigned int i = 0; i < importCount; ++i) {
			stream.read_u32(); // skip unknown
			stream.read_sstr(); // skip import path
		}

		origin = stream.read_sstr(); // read the target write path, at this point we dont need to read any further

		std::uint32_t hashed = aurora::fnv1a(std::format("A{}", origin)); // this is the .pc file to write to

		write_to_thumper_cache(hashed, stream.mBuffer);

		spdlog::info("Found .objlib: {}", key);
		break;
	}

	for (auto const& [key, value] : level.files) {
		if (!key.ends_with(".sec")) continue;

		auto path = std::filesystem::path(origin);
		auto hashInput = std::format("A{}/{}.sec", path.parent_path().generic_string(), path.stem().generic_string());

		std::uint32_t hashed = aurora::fnv1a(hashInput);

		write_to_thumper_cache(hashed, value);

		spdlog::info("Found .sec: {}", key);
		break;
	}

	// Process .pc files, these go directly into cache
	for (auto const& [key, value] : level.files) {
		if (!key.ends_with(".pc")) continue;
		std::string stem = std::filesystem::path(key).stem().generic_string();
		std::uint32_t hashed = std::stoul(stem, 0, 16);
		write_to_thumper_cache(hashed, value);
		spdlog::info("Found .pc: {}", key);
	}

	std::string locKey = std::format("custom.{}", modid);
	std::transform(locKey.begin(), locKey.end(), locKey.begin(), [](char c) { return std::tolower(c); });

	auto& localization = gModDb.localization["Aui/strings.en.loc"];
	auto hashed = aurora::fnv1a(locKey);
	localization[static_cast<LocalizationKey>(hashed)] = modid;

	gModDb.listings["Aui/thumper.levels"].entries.emplace_back(
		locKey,
		0,
		origin,
		"",
		false,
		false,
		false,
		0,
		10
	);
}

void load_mod(std::string const& modid) {
	spdlog::info("Loading `{}`", modid);

	// Apply direct files first
	if (std::filesystem::exists(std::format("mods/{}/direct", modid))) {
		for (auto const& entry : std::filesystem::recursive_directory_iterator(std::format("mods/{}/direct", modid))) {
			if (entry.is_directory()) continue;

			spdlog::info(entry.path().generic_string());

			// compute hash
			std::filesystem::path fspath = std::filesystem::relative(entry.path(), std::format("mods/{}/direct", modid)).generic_string();
			std::string path = std::format("A{}", fspath.generic_string());

			if (auto bytes = aurora::read_file(entry.path())) {
				std::uint32_t hashed = aurora::fnv1a(path);
				write_to_thumper_cache(hashed, *bytes);
			}
		}
	}

	if (std::filesystem::exists(std::format("mods/{}/localization", modid))) {
		for (auto const& entry : std::filesystem::recursive_directory_iterator(std::format("mods/{}/localization", modid))) {
			if (entry.is_directory()) continue;
			if (entry.path().extension().generic_string() != ".lua") continue;

			spdlog::info(entry.path().generic_string());

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
				spdlog::error("Lua Error: {}", lua_tostring(L, -1));
			}
			lua_pop(L, 1);
			lua_close(L);
		}
	}

	if (std::filesystem::exists(std::format("mods/{}/credits", modid))) {
		for (auto const& entry : std::filesystem::recursive_directory_iterator(std::format("mods/{}/credits", modid))) {
			if (entry.is_directory()) continue;
			if (entry.path().extension().generic_string() != ".lua") continue;

			spdlog::info(entry.path().generic_string());

			lua_State* L = luaL_newstate();
			if (luaL_dofile(L, entry.path().generic_string().c_str()) == LUA_OK) {
				std::filesystem::path fspath = std::filesystem::relative(entry.path(), std::format("mods/{}/credits", modid)).generic_string();
				std::string path = std::format("A{}/{}.credits", fspath.parent_path().generic_string(), fspath.stem().generic_string());

				Credits credits;
				credits.deserialize(L);

				gModDb.credits[path] = credits;
			}
			else {
				spdlog::error("Lua Error: {}", lua_tostring(L, -1));
			}
			lua_pop(L, 1);
			lua_close(L);
		}
	}

	if (std::filesystem::exists(std::format("mods/{}/levels", modid))) {
		for (auto const& entry : std::filesystem::recursive_directory_iterator(std::format("mods/{}/levels", modid))) {
			if (entry.is_directory()) continue;
			if (entry.path().extension().generic_string() != ".lua") continue;

			spdlog::info(entry.path().generic_string());

			lua_State* L = luaL_newstate();
			if (luaL_dofile(L, entry.path().generic_string().c_str()) == LUA_OK) {
				std::filesystem::path fspath = std::filesystem::relative(entry.path(), std::format("mods/{}/levels", modid)).generic_string();
				std::string path = std::format("A{}/{}.levels", fspath.parent_path().generic_string(), fspath.stem().generic_string());

				LevelListing credits;
				credits.deserialize(L);

				gModDb.listings[path] = credits;
			}
			else {
				spdlog::info(lua_tostring(L, -1));
			}
			lua_pop(L, 1);
			lua_close(L);
		}
	}

	if (std::filesystem::exists(std::format("mods/{}/textures", modid))) {
		for (auto const& entry : std::filesystem::recursive_directory_iterator(std::format("mods/{}/textures", modid))) {
			if (entry.is_directory()) continue;
			if (entry.path().extension().generic_string() != ".dds") continue;

			spdlog::info(entry.path().generic_string());

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

void find_mods() {
	gFoundMods.clear();

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
		
		if ((!std::filesystem::exists(std::filesystem::path("mods") / it->modid)) && (!std::filesystem::exists(std::filesystem::path("mods") / (it->modid + ".zip")))) {
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
		std::string modid;

		if (entry.is_directory()) {
			modid = entry.path().filename().generic_string();
		}
		else if (entry.path().extension().generic_string() == ".zip") {
			modid = entry.path().stem().generic_string();
		}
		else
			continue;

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

void restore_cache_content() {
	for (auto& entry : std::filesystem::directory_iterator("cache")) {
		std::string backupFile = std::format("{}.bak", entry.path().generic_string());
		if (std::filesystem::exists(backupFile)) {
			std::filesystem::copy_file(backupFile, entry.path(), std::filesystem::copy_options::overwrite_existing);
		}
	}
}

void build() {
	restore_cache_content(); // Always revert content BEFORE applying mods, this will catch some small issues when the base mods doesnt cover certain situations

	// Save mod order and enable flags
	save_mod_order_state();

	if (!std::filesystem::exists("mods/base")) {
		spdlog::info("Thumper content has not been unpacked");
		spdlog::info("Aurora cannot build mod content until this is done");
		return;
	}

	for (auto const& [modid, enabled] : gFoundMods) {
		if (!enabled) continue;
		if (std::filesystem::exists(std::filesystem::path("mods") / (modid + ".zip"))) continue; // is a zip, skip
		load_mod(modid);
	}

	for (auto const& [modid, enabled] : gFoundMods) {
		if (!enabled) continue;
		if (std::filesystem::exists(std::filesystem::path("mods") / (modid + ".zip"))) continue; // is a zip, skip
		process_mod_hooks(modid);
	}

	// process customs, this is ALWAYS done after native mods
	for (auto const& [modid, enabled] : gFoundMods) {
		if (!enabled) continue;
		if (!std::filesystem::exists(std::filesystem::path("mods") / (modid + ".zip"))) continue; // Not a zip/TCLE mod
		
		load_precompiled_tcle_mods(modid);
	}

	spdlog::info("Building assets");

	for (auto const& [key, table] : gModDb.localization) {
		spdlog::info("`{}`", key);
		std::vector<LocalizationEntry> enteries;

		std::uint32_t totalBytes = 0;

		for (auto& [key, value] : table) {
			LocalizationEntry entry;
			entry.key = static_cast<uint32_t>(key);
			entry.offset = totalBytes;
			entry.value = value;

			totalBytes += static_cast<std::uint32_t>(value.size() + 1ull);

			enteries.emplace_back(std::move(entry));
		}

		aurora::ByteStream stream;
		stream.write_u32(16);
		
		stream.write_u32(static_cast<std::uint32_t>(enteries.size()));
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
		spdlog::info("`{}`", key);

		aurora::ByteStream stream;
		stream.write_u32(16);
		table.serialize(stream);

		write_to_thumper_cache(aurora::fnv1a(key), stream.mBuffer);
	}

	for (auto& [key, table] : gModDb.listings) {
		spdlog::info("`{}`", key);

		aurora::ByteStream stream;
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

				aurora::ByteStream stream;
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

	spdlog::info("Done");
}

ImFont* gVariableSpace = nullptr;
ImFont* gMonoSpace = nullptr;

static bool gShouldLaunchThumper = false;

namespace aurora {
	bool should_launch_thumper() { return gShouldLaunchThumper; }
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
							aurora::spawn_process_with_path_argument(*mPathHxD, fmt::format("cache/{}", match.file));
						}
					}

					if (mPathImHex) {
						if (ImGui::MenuItem("Open File in ImHex")) {
							aurora::spawn_process_with_path_argument(*mPathImHex, fmt::format("cache/{}", match.file));
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
	spdlog::info(lua_tostring(L, 1));
	return 0;
}

void cache_scan(std::unordered_set<std::string>& pcFileStorage) {
	spdlog::info("Scanning cache...");

	pcFileStorage.clear();

	for (auto const& entry : std::filesystem::directory_iterator("cache")) {
		pcFileStorage.insert(entry.path().filename().generic_string());
	}

	spdlog::info(fmt::format("{} file(s)", pcFileStorage.size()));

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
			spdlog::error("Lua Error: {}", lua_tostring(L, -1));
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

enum struct Comp : std::uint32_t {
	EditStateComp = aurora::fnv1a("EditStateComp"),
	TimeDriveComp = aurora::fnv1a("TimeDriveComp"),
	AnimComp = aurora::fnv1a("AnimComp"),
	PollComp = aurora::fnv1a("PollComp"),
	StatusComp = aurora::fnv1a("StatusComp"),
	ControllerStatusComp = aurora::fnv1a("ControllerStatusComp"),
	DrawComp = aurora::fnv1a("DrawComp"),
};

#include "IconsFontAwesome6.h"

namespace aurora {
	void logger_init() {
		spdlog::default_logger()->sinks().emplace_back(std::make_shared<spdlog::sinks::basic_file_sink_mt>("aurora.log", true));
		spdlog::set_level(spdlog::level::trace);
		spdlog::flush_every(std::chrono::seconds(5));
		spdlog::flush_on(spdlog::level::err);
		spdlog::flush_on(spdlog::level::critical);
	}

#include <array>

void main() {
	logger_init();

	if (auto path = std::format("{}/ImHex/imhex-gui.exe", get_program_files_directory()); std::filesystem::exists(path)) {
		mPathImHex = path;
		spdlog::debug("Found ImHex");
	}

	if (auto path = std::format("{}/HxD/HxD.exe", get_program_files_directory()); std::filesystem::exists(path)) {
		mPathHxD = path;
		spdlog::debug("Found HxD");
	}

	bool toolsBinarySearch = false;
	
	cache_scan(pcFileStorage);

	aurora::Window window;
	
	{
		using stbi_deleter = aurora::DeleterOf<stbi_image_free>;
		int x, y;
		std::unique_ptr<stbi_uc, stbi_deleter> pixels = decltype(pixels)(stbi_load("aurora/icon.png", &x, &y, nullptr, 4));

		window = { {
				.title = "Aurora v0.0.4-a.11",
				.iconPixels = pixels.get(),
				.iconWidth = x,
				.iconHeight = y,
		} };
	}

	PluginEngine pluginEngine;
	pluginEngine.reload();

	IMGUI_CHECKVERSION();
	ImGui::CreateContext();
	ImGuiIO& io = ImGui::GetIO();
	io.ConfigFlags |= ImGuiConfigFlags_NavEnableKeyboard;
	io.ConfigFlags |= ImGuiConfigFlags_DockingEnable;

	gVariableSpace = io.Fonts->AddFontFromFileTTF("aurora/NotoSans-Regular.ttf", 18.0f);

	ImFontConfig config;
	config.MergeMode = true;
	config.GlyphMinAdvanceX = 18.0f; // Use if you want to make the icon monospaced
	io.Fonts->AddFontFromFileTTF("aurora/fa-solid-900.ttf", 18.0f, &config);

	gMonoSpace = io.Fonts->AddFontFromFileTTF("aurora/NotoSansMono-Regular.ttf", 18.0f);

	ImGui::StyleColorsDark();
	ImGui_ImplGlfw_InitForOpenGL(window.handle(), true);
	ImGui_ImplOpenGL3_Init("#version 450 core");

	spdlog::info("Welcome to Aurora!");

	bool showDemo = false;

	aurora::GuiHasher hasher;
	bool hasherVisible = false;

	find_mods();

	bool buildModContent = true;
	bool viewUnpackGui = false;

	while (!glfwWindowShouldClose(window.handle())) {
		glfwPollEvents();

		ImGui_ImplOpenGL3_NewFrame();
		ImGui_ImplGlfw_NewFrame();
		ImGui::NewFrame();

		glEnable(GL_DEPTH_TEST);

		ImGui::DockSpaceOverViewport();

		if (ImGui::BeginMainMenuBar()) {
			if (ImGui::BeginMenu("View")) {
				ImGui::MenuItem("Hasher", nullptr, &hasherVisible);
				ImGui::MenuItem("Unpacker", nullptr, &viewUnpackGui);
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

			if (ImGui::BeginMenu("Experimental")) {
				ImGui::MenuItem("Objlib Level Parser", nullptr, &gObjlibParserStaticDataVisible);

				ImGui::EndMenu();
			}
		}

		ImGui::EndMainMenuBar();

		unpack_gui(viewUnpackGui);
		

		static std::string path = "Alevels/demo.objlib";

		if (gObjlibParserStaticDataVisible) {
			if (ImGui::Begin("Objlib Parser", &gObjlibParserStaticDataVisible)) {
				
				static std::string status = "Waiting";

				ImGui::InputText("Path", &path);

				ImGui::SameLine();

				if (ImGui::Button("Parse")) {
					try {
						status = "Parsing...";
						gObjlibParserStaticData.parse(path);
						status = "OK";
					}
					catch (AuroraParseError const& e) {
						status = e.what();
					}
				}

				ImGui::Text("%s", status.c_str());

				ImGui::Separator();

				gObjlibParserStaticData.gui();

			}
			ImGui::End();

			gObjlibParserStaticData.guiEditors();
		}
		

		static std::string selectionContext;

		static std::once_flag flag; std::call_once(flag, ImGui::SetNextWindowFocus);
		ImGui::SetNextWindowPos(ImVec2(io.DisplaySize.x * 0.5f, io.DisplaySize.y * 0.5f), ImGuiCond_Once, ImVec2(0.5f, 0.5f));
		ImGui::SetNextWindowSize(ImVec2(800, 600), ImGuiCond_Once);
		if (ImGui::Begin("Launcher", nullptr, ImGuiWindowFlags_MenuBar)) {
			
			if (ImGui::BeginMenuBar()) {

				if (ImGui::BeginMenu("File")) {
					if (ImGui::MenuItem("Enable All")) {
						for (auto& item : gFoundMods) item.enabled = true;
					}

					if (ImGui::MenuItem("Disable All")) {
						for (auto& item : gFoundMods) item.enabled = false;
					}

					ImGui::Separator();

					if (ImGui::MenuItemEx("Open Mods Directory", ICON_FA_FOLDER_OPEN)) {
						ShellExecuteA(nullptr, "explore", (std::filesystem::current_path() / "mods").generic_string().c_str(), NULL, NULL, SW_SHOWDEFAULT);
					}

					if (ImGui::MenuItemEx("Rescan Mods Directory", ICON_FA_ROTATE)) {
						save_mod_order_state();
						find_mods();
					}

					ImGui::EndMenu();
				}

				ImGui::EndMenuBar();
			}

			ImGui::Columns(2);

			float footerSize = ImGui::GetStyle().ItemSpacing.y + ImGui::GetFrameHeightWithSpacing();

			ImGui::SeparatorText("Inactive");
			if (ImGui::BeginChild("Inactive", { 0.0f, -footerSize }, ImGuiChildFlags_None, ImGuiWindowFlags_AlwaysVerticalScrollbar)) {
				for (auto& item : gFoundMods) {
					if (item.enabled) continue;

					std::underlying_type_t<ImGuiTreeNodeFlags_> flags = ImGuiTreeNodeFlags_Leaf | ImGuiTreeNodeFlags_SpanAvailWidth;
					if (selectionContext == item.modid) { flags |= ImGuiTreeNodeFlags_Selected; }

					std::optional<char const*> issue = std::nullopt;

					if (item.modid == "base") {
						issue = "WARN: `base` is not being loaded. This will cause loss of base game content.";
					}

					if (issue) ImGui::PushStyleColor(ImGuiCol_Text, { 1.0f, fmodf(glfwGetTime(), 1.0f), fmodf(glfwGetTime(), 1.0f), 1.0f });

					if (ImGui::TreeNodeEx(item.modid.c_str(), flags)) ImGui::TreePop();

					if (issue) {
						ImGui::PushStyleColor(ImGuiCol_Text, { 1.0f, 0.0f, 0.0f, 1.0f });
						ImGui::SetItemTooltip("%s", *issue);
						ImGui::PopStyleColor(2);
					}

					if (ImGui::IsItemActivated()) { selectionContext = item.modid; }

					if (ImGui::IsMouseDoubleClicked(ImGuiMouseButton_Left) && ImGui::IsItemHovered(ImGuiHoveredFlags_None)) {
						item.enabled ^= true;
					}
				}
			}

			ImGui::EndChild();

			ImGui::NextColumn();

			ImGui::SeparatorText("Active");
			if (ImGui::BeginChild("Active", { 0.0f, -footerSize }, ImGuiChildFlags_None, ImGuiWindowFlags_AlwaysVerticalScrollbar)) {
				for (std::size_t n = 0; n < gFoundMods.size(); ++n) {
					auto& item = gFoundMods[n];
					if (!item.enabled) continue;

					std::underlying_type_t<ImGuiTreeNodeFlags_> flags = ImGuiTreeNodeFlags_Leaf | ImGuiTreeNodeFlags_SpanAvailWidth;
					if (selectionContext == item.modid) { flags |= ImGuiTreeNodeFlags_Selected; }

					std::optional<char const*> issue = std::nullopt;

					if (n == 0 && gFoundMods[n].modid != "base") {
						issue = "WARN: `base` is not the first loaded mod in the list. This will cause loss of base game content.";
					}
					else if (n != 0 && gFoundMods[n].modid == "base") {
						issue = "WARN: `base` is not the first loaded mod in the list. This will cause loss of base game content.";
					}
					else if (item.modid == "aurora.base" && n != 1) {
						issue = "WARN: `aurora.base` is not loaded directly after `base`. This will cause loss of base aurora content.";
					}

					if (issue) ImGui::PushStyleColor(ImGuiCol_Text, { 1.0f, fmodf(glfwGetTime(), 1.0f), fmodf(glfwGetTime(), 1.0f), 1.0f });

					// A duplicate id may be present for one frame when switching state from inactive -> active
					ImGui::PushItemFlag(ImGuiItemFlags_AllowDuplicateId, true);
					if (ImGui::TreeNodeEx(item.modid.c_str(), flags)) ImGui::TreePop();

					if (issue) {
						ImGui::PushStyleColor(ImGuiCol_Text, { 1.0f, 0.0f, 0.0f, 1.0f });
						ImGui::SetItemTooltip("%s", *issue);
						ImGui::PopStyleColor(2);
					}

					

					//if (n == 1 && gFoundMods[n].modid != "aurora.base") {
					//	auto* window = ImGui::GetCurrentWindow();
					//	ImGui::GetForegroundDrawList(window)->AddRect(window->Pos, window->Pos + window->Size, IM_COL32(255, 255, 0, 255));
					//	ImGui::SetItemTooltip("%s", "`aurora.base` is not the second loaded mod in the list. This may cause issues when applying mods.");
					//}

					ImGui::PopItemFlag();

					if (ImGui::IsItemActivated()) { selectionContext = item.modid; }

					if (ImGui::IsMouseDoubleClicked(ImGuiMouseButton_Left) && ImGui::IsItemHovered(ImGuiHoveredFlags_None)) {
						item.enabled ^= true;
					}

					if (ImGui::BeginDragDropSource()) {
						ImGui::SetDragDropPayload("modid_reorder", &n, sizeof(std::size_t));
						ImGui::TextUnformatted(item.modid.c_str());
						ImGui::EndDragDropSource();
					}

					if (ImGui::BeginDragDropTarget()) {
						if (ImGuiPayload const* payload = ImGui::AcceptDragDropPayload("modid_reorder")) {
							IM_ASSERT(payload->DataSize == sizeof(std::size_t));
							std::size_t const sourceIdx = *reinterpret_cast<std::size_t const*>(payload->Data);
							std::swap(gFoundMods[sourceIdx], gFoundMods[n]);
						}

						ImGui::EndDragDropTarget();
					}
				}
			}
			ImGui::EndChild();


			ImGui::Columns(1);

			ImGui::Separator();			

			ImGui::Checkbox("Build Mod Content", &buildModContent);

			ImGui::SameLine();

			if (ImGui::Button("Launch Thumper")) {
				if (buildModContent) {
					build();
				}

				glfwSetWindowShouldClose(window.handle(), true);
				gShouldLaunchThumper = true;
			}
		}
		ImGui::End();

		if (ImGui::Begin("Mod Details")) {
			if (!selectionContext.empty()) {
				ModEntry* entry = nullptr;

				for (auto& item : gFoundMods) {
					if (item.modid == selectionContext) {
						entry = &item;
						break;
					}
				}

				assert(entry);

				ImGui::TextUnformatted(entry->modid.c_str());
			}
		}
		ImGui::End();

		hasher.on_gui(hasherVisible, gHashtable);

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

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		ImGui::Render();
		int display_w, display_h;
		glfwGetFramebufferSize(window.handle(), &display_w, &display_h);
		glViewport(0, 0, display_w, display_h);
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
		ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());

		glfwSwapBuffers(window.handle());
	}

	pluginEngine.shutdown();

	ImGui_ImplOpenGL3_Shutdown();
	ImGui_ImplGlfw_Shutdown();
	ImGui::DestroyContext();
}
}
