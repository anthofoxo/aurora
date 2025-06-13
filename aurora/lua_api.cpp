#include "lua_api.hpp"

#include <filesystem>

#include <imgui.h>
#include <misc/cpp/imgui_stdlib.h>

#include "au_util.hpp"
#include "api/au_api.hpp"

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
}

#define DDSKTX_IMPLEMENT
#include "dds-ktx.h"
#include <iostream>

void aurora::register_plugin_api(lua_State* L) {
	lua_newtable(L);
	lua_pushcfunction(L, &lua_hash);
	lua_setfield(L, -2, "hash");
	lua_pushcfunction(L, &lua_string_unescape);
	lua_setfield(L, -2, "unescape");
	lua_pushcfunction(L, &lua_escape);
	lua_setfield(L, -2, "escape");

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

	aurora::api_register_filesystem(L);
	lua_setfield(L, -2, "filesystem");

	lua_setglobal(L, "Aurora");

	aurora::api_register_imgui(L);
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
#define AU_IMPL_GL_EXPAND(L, name) lua_pushinteger(L, GL_ ## name); lua_setfield(L, -2, #name)
	AU_IMPL_GL_EXPAND(L, TEXTURE_2D);
	AU_IMPL_GL_EXPAND(L, RGBA8);
#undef AU_IMPL_GL_EXPAND
	lua_setglobal(L, "GL");
}