#include "lua_api.hpp"

#include <filesystem>

#include <imgui.h>
#include <misc/cpp/imgui_stdlib.h>

#include "au_util.hpp"

#include <glad/gl.h>

namespace {
	int lua_hash(lua_State* L) {
		size_t len;
		char const* data = lua_tolstring(L, 1, &len);
		std::uint32_t const result = aurora::hash(std::span(reinterpret_cast<std::byte const*>(data), len));
		lua_pushinteger(L, result);
		return 1;
	}

	int lua_string_unescape(lua_State* L) {
		std::size_t size;
		char const* bytes = luaL_checklstring(L, 1, &size);
		std::string const unescaped = aurora::unescape(std::string_view(bytes, size));
		lua_pushlstring(L, unescaped.data(), unescaped.size());
		return 1;
	}

	int lua_escape(lua_State* L) {
		std::size_t size;
		char const* bytes = luaL_checklstring(L, 1, &size);
		std::string const escaped = aurora::escape(std::string_view(bytes, size));
		lua_pushlstring(L, escaped.data(), escaped.size());
		return 1;
	}

	// ReSharper disable once CppDFAConstantFunctionResult
	int aurora_directory_iterator(lua_State* L) {
		char const* directory = luaL_checkstring(L, 1);

		if (!std::filesystem::exists(directory)) {
			lua_pushnil(L);
			return 1;
		}

		lua_newtable(L);
		lua_Integer index = 1;

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

	int imgui_begin_menu(lua_State* L) {
		lua_pushboolean(L, ImGui::BeginMenu(luaL_checkstring(L, 1)));
		return 1;
	}

	int imgui_end_menu(lua_State* L) {
		ImGui::EndMenu();
		return 0;
	};

	int imgui_separator(lua_State* L) {
		ImGui::Separator();
		return 0;
	}
	
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

	int imgui_sameline(lua_State* L) {
		ImGui::SameLine();
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

	int imgui_label_text(lua_State* L) {
		char const* label = luaL_checkstring(L, 1);
		int numArgs = lua_gettop(L);
		lua_getglobal(L, "string");
		lua_getfield(L, -1, "format");
		lua_remove(L, -2);
		lua_insert(L, 2);
		lua_pcall(L, numArgs - 1, 1, 0);
		ImGui::LabelText(label, "%s", lua_tostring(L, -1));
		return 0;
	}

	int imgui_TextColored(lua_State* L) {
		lua_rawgeti(L, 1, 1);
		float r = lua_tonumber(L, -1);
		lua_pop(L, 1);

		lua_rawgeti(L, 1, 2);
		float g = lua_tonumber(L, -1);
		lua_pop(L, 1);

		lua_rawgeti(L, 1, 3);
		float b = lua_tonumber(L, -1);
		lua_pop(L, 1);

		lua_rawgeti(L, 1, 4);
		float a = lua_tonumber(L, -1);
		lua_pop(L, 1);

		int numArgs = lua_gettop(L);
		lua_getglobal(L, "string");
		lua_getfield(L, -1, "format");
		lua_remove(L, -2);
		lua_insert(L, 2);
		lua_pcall(L, numArgs - 1, 1, 0);

		ImGui::TextColored({ r, g, b, a }, "%s", lua_tostring(L, -1));

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

			bool value = ImGui::MenuItem(label, nullptr, &selected, true);

			lua_pushboolean(L, selected);
			lua_rawseti(L, 3, 1);

			lua_pushboolean(L, value);
			return 1;
	}

	int imgui_PushStyleVar(lua_State* L) {
		if (lua_isnumber(L, 2)) {
			ImGui::PushStyleVar(lua_tointeger(L, 1), lua_tonumber(L, 2));
		}
		else if (lua_istable(L, 2)) {
			lua_rawgeti(L, 2, 1);
			float x = lua_tonumber(L, -1);
			lua_pop(L, 1);
			lua_rawgeti(L, 2, 2);
			float y = lua_tonumber(L, -1);
			lua_pop(L, 1);

			ImGui::PushStyleVar(lua_tointeger(L, 1), { x, y });
		}
		else
			luaL_typeerror(L, 2, "table|number");

		return 0;
	}

	int imgui_PopStyleVar(lua_State* L) {
		ImGui::PopStyleVar();
		return 0;
	}
}

#define DDSKTX_IMPLEMENT
#include "dds-ktx.h"
#include <iostream>

void aurora::register_plugin_api(lua_State* L) {
	lua_newtable(L);
	lua_pushcfunction(L, &aurora_directory_iterator);
	lua_setfield(L, -2, "directory_iterator");
	lua_pushcfunction(L, &aurora_is_regular_file);
	lua_setfield(L, -2, "is_regular_file");
	lua_pushcfunction(L, &lua_hash);
	lua_setfield(L, -2, "hash");
	lua_pushcfunction(L, &lua_string_unescape);
	lua_setfield(L, -2, "unescape");
	lua_pushcfunction(L, &lua_escape);
	lua_setfield(L, -2, "escape");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		lua_pushboolean(L, std::filesystem::create_directory(luaL_checkstring(L, 1)));
		return 1;
	});
	lua_setfield(L, -2, "create_directory");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		lua_pushboolean(L, std::filesystem::create_directories(luaL_checkstring(L, 1)));
		return 1;
	});
	lua_setfield(L, -2, "create_directories");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		char const* path = luaL_checkstring(L, 1);

		auto const bytes = read_file(path);

		if (!bytes.has_value()) {
			lua_pushnil(L);
		}
		else {
			lua_pushlstring(L, reinterpret_cast<const char *>(bytes->data()), bytes->size());
		}

		return 1;
	});
	lua_setfield(L, -2, "read_file");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		char const* path = luaL_checkstring(L, 1);

		std::size_t size;
		char const* data = luaL_checklstring(L, 2, &size);

		bool const success = write_file(path, std::as_bytes(std::span(data, size)));
		lua_pushboolean(L, success);

		return 1;
	});
	lua_setfield(L, -2, "write_file");

	lua_pushcfunction(L, [](lua_State* L)-> int {
		std::size_t size;
		char const* data = luaL_checklstring(L, 1, &size);

		ddsktx_texture_info tc = { 0 };
		ddsktx_error error;

		if (ddsktx_parse(&tc, data, size, &error)) {
			GLuint texture;
			glCreateTextures(GL_TEXTURE_2D, 1, &texture);
			GLenum format;

			switch (tc.format) {
				case DDSKTX_FORMAT_BC1:
					format = GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
					break;
				case DDSKTX_FORMAT_BC2:
					format = GL_COMPRESSED_RGBA_S3TC_DXT3_EXT;
					break;
				case DDSKTX_FORMAT_BC3:
					format = GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
					break;
				case DDSKTX_FORMAT_BGRA8:
					format = GL_BGRA;
					break;
				default:
					luaL_error(L, "Unsupported dds texture format: %d", tc.format);
					return 1;
			}

			ddsktx_sub_data subdata;
			ddsktx_get_sub(&tc, &subdata, data, size, 0, 0, 0);

			glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

			glTextureStorage2D(texture, 1, format, subdata.width, subdata.height);
			if (ddsktx_format_compressed(tc.format)) {
				glCompressedTextureSubImage2D(texture, 0, 0, 0, subdata.width, subdata.height, format, subdata.size_bytes, subdata.buff);
			}
			else {
				glTextureSubImage2D(texture, 0, 0, 0, subdata.width, subdata.height, format, GL_UNSIGNED_BYTE, subdata.buff);
			}

			glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

			lua_pushinteger(L, texture);
			return 1;
		}
		else {
			std::cout << error.msg << std::endl;
			lua_pushnil(L);
		}

		return 1;
	});
	lua_setfield(L, -2, "ddsktx_parse");

	lua_setglobal(L, "Aurora");

	lua_newtable(L);
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
	lua_pushcfunction(L, &imgui_label_text);
	lua_setfield(L, -2, "LabelText");
	lua_pushcfunction(L, &imgui_text_wrapped);
	lua_setfield(L, -2, "TextWrapped");
	lua_pushcfunction(L, &imgui_menu_item);
	lua_setfield(L, -2, "MenuItem");
	lua_pushcfunction(L, &imgui_button);
	lua_setfield(L, -2, "Button");
	lua_pushcfunction(L, &imgui_separator);
	lua_setfield(L, -2, "Separator");
	lua_pushcfunction(L, &imgui_sameline);
	lua_setfield(L, -2, "SameLine");
	lua_pushcfunction(L, &imgui_TextColored);
	lua_setfield(L, -2, "TextColored");
	lua_pushcfunction(L, &imgui_PushStyleVar);
	lua_setfield(L, -2, "PushStyleVar");
	lua_pushcfunction(L, &imgui_PopStyleVar);
	lua_setfield(L, -2, "PopStyleVar");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		char const *id = luaL_checkstring(L, 1);
		int const numCols = luaL_checkinteger(L, 2);
		lua_pushboolean(L, ImGui::BeginTable(id, numCols));
		return 1;
	});
	lua_setfield(L, -2, "BeginTable");
	lua_pushcfunction(L, []([[maybe_unused]] lua_State *L) -> int {
		ImGui::EndTable();
		return 0;
	});
	lua_setfield(L, -2, "EndTable");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		ImGui::TableNextRow();
		return 0;
	});
	lua_setfield(L, -2, "TableNextRow");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		ImGui::TableSetColumnIndex(luaL_checkinteger(L, 1));
		return 0;
	});
	lua_setfield(L, -2, "TableSetColumnIndex");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		ImGui::Columns(luaL_checkinteger(L, 1));
		return 0;
	});
	lua_setfield(L, -2, "Columns");

	lua_pushcfunction(L, []([[maybe_unused]] lua_State *L) -> int {
		ImGui::NextColumn();
		return 0;
	});
	lua_setfield(L, -2, "NextColumn");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		lua_pushboolean(L, ImGui::BeginChild(luaL_checkstring(L, 1)));
		return 1;
	});
	lua_setfield(L, -2, "BeginChild");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		ImGui::EndChild();
		return 0;
	});
	lua_setfield(L, -2, "EndChild");

	lua_pushcfunction(L, [](lua_State *L)-> int {
		auto const image = static_cast<ImTextureID>(static_cast<std::uintptr_t>(luaL_checkinteger(L, 1)));

		lua_rawgeti(L, 2, 1);
		float const width = lua_tonumber(L, -1);
		lua_pop(L, 1);

		lua_rawgeti(L, 2, 2);
		float const height = lua_tonumber(L, -1);
		lua_pop(L, 1);

		ImGui::Image(image, {width, height });

		return 0;
	});
	lua_setfield(L, -2, "Image");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		char const *label = luaL_checkstring(L, 1);

		bool selected = false;
		if (lua_gettop(L) == 2) { selected = lua_toboolean(L, 2); }

		lua_pushboolean(L, ImGui::Selectable(label, selected));
		return 1;
	});
	lua_setfield(L, -2, "Selectable");

	lua_setglobal(L, "ImGui");

	lua_newtable(L);
	lua_pushcfunction(L, [](lua_State *L) -> int {
		GLuint handle;
		glCreateBuffers(1, &handle);
		lua_pushinteger(L, handle);
		return 1;
	});
	lua_setfield(L, -2, "CreateBuffers");
	lua_pushcfunction(L, [](lua_State *L) -> int {
		GLuint const handle = luaL_checkinteger(L, 1);
		glDeleteBuffers(1, &handle);
		return 0;
	});
	lua_setfield(L, -2, "DeleteBuffers");
	lua_pushcfunction(L, [](lua_State *L) -> int {
		GLuint const buffer = luaL_checkinteger(L, 1);
		GLsizeiptr const size = luaL_checkinteger(L, 2);
		char const* data = luaL_checkstring(L, 3);
		GLbitfield const flags = luaL_checkinteger(L, 4);
		glNamedBufferStorage(buffer, size, data, flags);
		return 0;
	});
	lua_setfield(L, -2, "NamedBufferStorage");
	lua_pushcfunction(L, [](lua_State *L) -> int {
		GLuint handle;
		glCreateVertexArrays(1, &handle);
		lua_pushinteger(L, handle);
		return 1;
	});
	lua_setfield(L, -2, "CreateVertexArrays");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		GLenum const target = luaL_checkinteger(L, 1);
		GLuint handle;
		glCreateTextures(target, 1, &handle);
		lua_pushinteger(L, handle);
		return 1;
	});
	lua_setfield(L, -2, "CreateTextures");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		GLuint const handle = luaL_checkinteger(L, 1);
		glDeleteTextures(1, &handle);
		return 0;
	});
	lua_setfield(L, -2, "DeleteTextures");

	lua_pushcfunction(L, [](lua_State *L) -> int {
		GLuint const texture = luaL_checkinteger(L, 1);
		GLsizei const levels = luaL_checkinteger(L, 2);
		GLenum const internalformat = luaL_checkinteger(L, 3);
		GLsizei const width = luaL_checkinteger(L, 4);
		GLsizei const height = luaL_checkinteger(L, 5);
		glTextureStorage2D(texture, levels, internalformat, width, height);
		return 0;
	});
	lua_setfield(L, -2, "TextureStorage2D");

	lua_setglobal(L, "gl");

	lua_newtable(L);
	lua_pushinteger(L, GL_TEXTURE_2D); lua_setfield(L, -2, "TEXTURE_2D");
	lua_pushinteger(L, GL_RGBA8); lua_setfield(L, -2, "RGBA8");
	lua_setglobal(L, "GL");
}