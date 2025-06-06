#include "lua_api.hpp"

#include <filesystem>

#include <imgui.h>
#include <misc/cpp/imgui_stdlib.h>

namespace {
	int lua_string_unescape(lua_State* L) {
		std::size_t size;
		char const* bytes = luaL_checklstring(L, 1, &size);
		std::string text = std::string(bytes, size);

		std::stringstream output;

		for (size_t i = 0; i < text.length(); ++i) {
			if (text[i] == '\\' && i + 3 < text.length() && text[i + 1] == 'x') {
				std::stringstream hex_value;
				hex_value << text[i + 2] << text[i + 3]; // Exactly 2 hex digits

				unsigned int value;
				hex_value >> std::hex >> value;
				output << static_cast<char>(value);

				i += 3; // Skip \xHH (4 characters total)
			}
			else {
				output << text[i];
			}
		}

		text = output.str();

		lua_pushlstring(L, text.data(), text.size());
		return 1;
	}

	int aurora_directory_iterator(lua_State* L) {
		char const* directory = luaL_checkstring(L, 1);

		if (!std::filesystem::exists(directory)) {
			lua_pushnil(L);
			return 1;
		}

		lua_newtable(L);
		std::size_t index = 1;

		for(auto const& entry : std::filesystem::directory_iterator(directory)) {
			std::u8string const string = entry.path().generic_u8string();
			lua_pushlstring(L, reinterpret_cast<char const*>(string.data()), std::distance(string.begin(), string.end()));
			lua_rawseti(L, -2, index++);
		}

		return 1;
	}

	int aurora_is_regular_file(lua_State* L) {
		lua_pushboolean(L, std::filesystem::is_regular_file(luaL_checkstring(L, 1)));
		return 1;
	}

	int aurora_throw(lua_State* L) {
		throw std::runtime_error(lua_tostring(L, -1));
		return 0;
	}

	int imgui_begin(lua_State* L) {
		char const* title = luaL_checkstring(L, 1);

		lua_rawgeti(L, 2, 1);
		bool open = lua_toboolean(L, -1);
		lua_pop(L, 1);

		bool returnValue = ImGui::Begin(title, &open);

		lua_pushboolean(L, open);
		lua_rawseti(L, 2, 1);

		lua_pushboolean(L, returnValue);
		return 1;
	}

	int imgui_end(lua_State* L) {
		ImGui::End();
		return 0;
	}

	int imgui_begin_main_menu_bar(lua_State* L) {
		lua_pushboolean(L, ImGui::BeginMainMenuBar());
		return 1;
	}

	int imgui_end_main_menu_bar(lua_State* L) {
		ImGui::EndMainMenuBar();
		return 0;
	}

	int imgui_begin_menu(lua_State* L) {
		lua_pushboolean(L, ImGui::BeginMenu(luaL_checkstring(L, 1)));
		return 1;
	}

	int imgui_end_menu(lua_State* L) {
		ImGui::EndMenu();
		return 0;
	};
	
	int imgui_separator_text(lua_State* L) {
		ImGui::SeparatorText(luaL_checkstring(L, 1));
		return 0;
	}

	int imgui_button(lua_State* L) {
		lua_pushboolean(L, ImGui::Button(luaL_checkstring(L, 1)));
		return 1;
	}

	int imgui_text_unformatted(lua_State* L) {
		ImGui::TextUnformatted(luaL_checkstring(L, 1));
		return 0;
	}

	int imgui_input_text(lua_State* L) {
		lua_rawgeti(L, 2, 1);
		std::string text = lua_tostring(L, -1);
		lua_pop(L, 1);
		bool result = ImGui::InputText(luaL_checkstring(L, 1), &text);
		lua_pushstring(L, text.c_str());
		lua_rawseti(L, 2, 1);

		lua_pushboolean(L, result);
		return 1;
	}

	int imgui_text(lua_State* L) {
		int numArgs = lua_gettop(L);
		lua_getglobal(L, "string");
		lua_getfield(L, -1, "format");
		lua_remove(L, -2);
		lua_insert(L, 1);
		lua_pcall(L, numArgs, 1, 0);
		ImGui::TextUnformatted(lua_tostring(L, -1));
		return 0;
	}

	int imgui_bullet_text(lua_State* L) {
		int numArgs = lua_gettop(L);
		lua_getglobal(L, "string");
		lua_getfield(L, -1, "format");
		lua_remove(L, -2);
		lua_insert(L, 1);
		lua_pcall(L, numArgs, 1, 0);
		ImGui::BulletText("%s", lua_tostring(L, -1));
		return 0;
	}

	int imgui_text_wrapped(lua_State* L) {
		int numArgs = lua_gettop(L);
		lua_getglobal(L, "string");
		lua_getfield(L, -1, "format");
		lua_remove(L, -2);
		lua_insert(L, 1);
		lua_pcall(L, numArgs, 1, 0);
		ImGui::TextWrapped("%s", lua_tostring(L, -1));
		return 0;
	}
	
	int imgui_menu_item(lua_State* L) {
		char const* label = luaL_checkstring(L, 1);
			// idx 2 unused
			// idx 4 unused
			
			lua_rawgeti(L, 3, 1);
			bool selected = lua_toboolean(L, -1);
			lua_pop(L, 1);

			bool returnvalue = ImGui::MenuItem(label, nullptr, &selected, true);

			lua_pushboolean(L, selected);
			lua_rawseti(L, 3, 1);

			lua_pushboolean(L, returnvalue);
			return 1;
	}
}

void aurora::register_plugin_api(lua_State* L) {
	lua_newtable(L);
	lua_pushcfunction(L, &aurora_directory_iterator);
	lua_setfield(L, -2, "directory_iterator");
	lua_pushcfunction(L, &aurora_is_regular_file);
	lua_setfield(L, -2, "is_regular_file");
	lua_pushcfunction(L, &aurora_throw);
	lua_setfield(L, -2, "throw");
	lua_pushcfunction(L, &lua_string_unescape);
	lua_setfield(L, -2, "unescape");
	lua_setglobal(L, "Aurora");

	lua_newtable(L);
	lua_pushcfunction(L, &imgui_begin);
	lua_setfield(L, -2, "Begin");
	lua_pushcfunction(L, &imgui_end);
	lua_setfield(L, -2, "End");
	lua_pushcfunction(L, &imgui_begin_main_menu_bar);
	lua_setfield(L, -2, "BeginMainMenuBar");
	lua_pushcfunction(L, &imgui_end_main_menu_bar);
	lua_setfield(L, -2, "EndMainMenuBar");
	lua_pushcfunction(L, &imgui_begin_menu);
	lua_setfield(L, -2, "BeginMenu");
	lua_pushcfunction(L, &imgui_end_menu);
	lua_setfield(L, -2, "EndMenu");
	lua_pushcfunction(L, &imgui_separator_text);
	lua_setfield(L, -2, "SeparatorText");
	lua_pushcfunction(L, &imgui_text_unformatted);
	lua_setfield(L, -2, "TextUnformatted");
	lua_pushcfunction(L, &imgui_input_text);
	lua_setfield(L, -2, "InputText");
	lua_pushcfunction(L, &imgui_text);
	lua_setfield(L, -2, "Text");
	lua_pushcfunction(L, &imgui_bullet_text);
	lua_setfield(L, -2, "BulletText");
	lua_pushcfunction(L, &imgui_text_wrapped);
	lua_setfield(L, -2, "TextWrapped");
	lua_pushcfunction(L, &imgui_menu_item);
	lua_setfield(L, -2, "MenuItem");
	lua_pushcfunction(L, &imgui_button);
	lua_setfield(L, -2, "Button");
	lua_setglobal(L, "ImGui");
}