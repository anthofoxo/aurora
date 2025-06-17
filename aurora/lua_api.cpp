#include "lua_api.hpp"

#include <bit>
#include <filesystem>
#include <optional>

#include <imgui.h>
#include <misc/cpp/imgui_stdlib.h>
#include <glm/glm.hpp>

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

struct MemoryBuffer final {
	std::vector<std::byte> mBuffer;
	std::size_t mCursor = 0;

	[[nodiscard]] std::size_t cursor() const { return mCursor; }
	[[nodiscard]] std::size_t size() const { return mBuffer.size(); }
	[[nodiscard]] std::size_t capacity() const { return mBuffer.capacity(); }

	[[nodiscard]] bool check(std::size_t const count) const { return mCursor + count <= mBuffer.size(); }

	template<typename T>
	void put(T value) {
		for (int i = 0; i < sizeof(value); ++i) { mBuffer.push_back(static_cast<std::byte>(0)); }
		*reinterpret_cast<decltype(value)*>(mBuffer.data() + mBuffer.size() - sizeof(value)) = value;
	}

	void close() { mBuffer = decltype(mBuffer)(); mCursor = 0; }

	void* pointer() { return mBuffer.data() + mCursor; }

	void resize(std::size_t size) { mBuffer.resize(size); }
	void reserve(std::size_t size) { mBuffer.reserve(size); }

	bool skip(std::size_t size) {
		if (!check(size)) return false;
		mCursor += size;
		return true;
	}

	template<typename T>
	std::optional<T> get() {
		if (!check(sizeof(T))) return std::nullopt;
		T value = *reinterpret_cast<T*>(mBuffer.data() + mCursor);
		mCursor += sizeof(T);
		return value;
	}
};

void aurora::register_plugin_api(lua_State* L) {
	lua_newtable(L);
	lua_pushcfunction(L, &lua_hash);
	lua_setfield(L, -2, "hash");
	lua_pushcfunction(L, &lua_string_unescape);
	lua_setfield(L, -2, "unescape");
	lua_pushcfunction(L, &lua_escape);
	lua_setfield(L, -2, "escape");

	lua_pushcfunction(L, [](lua_State *L) -> int {
			lua_pushnumber(L, std::bit_cast<float>(static_cast<std::uint32_t>(lua_tointeger(L, 1))));
			return 1;
		});
	lua_setfield(L, -2, "bitcast_float");

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

	aurora::api_register_glm(L);
	lua_setglobal(L, "glm");

	{


		lua_newtable(L);

		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto* memoryBuffer = static_cast<MemoryBuffer*>(lua_newuserdata(L, sizeof(MemoryBuffer)));
			new (memoryBuffer) MemoryBuffer();
			lua_newtable(L);
			lua_getglobal(L, "MemoryBuffer");
			lua_setfield(L, -2, "__index");
			lua_pushcfunction(L, [](lua_State *L) -> int { static_cast<MemoryBuffer*>(lua_touserdata(L, 1))->~MemoryBuffer(); return 0; });
			lua_setfield(L, -2, "__gc");
			lua_setmetatable(L, -2);
			return 1;
		});
		lua_setfield(L, -2, "new");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto& memoryBuffer = *static_cast<MemoryBuffer*>(lua_touserdata(L, 1));
			lua_pushinteger(L, memoryBuffer.size());
			return 1;
		});
		lua_setfield(L, -2, "size");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto& memoryBuffer = *static_cast<MemoryBuffer*>(lua_touserdata(L, 1));
			lua_pushinteger(L, memoryBuffer.capacity());
			return 1;
		});
		lua_setfield(L, -2, "capacity");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto& memoryBuffer = *static_cast<MemoryBuffer*>(lua_touserdata(L, 1));
			lua_pushinteger(L, memoryBuffer.cursor());
			return 1;
		});
		lua_setfield(L, -2, "cursor");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto& memoryBuffer = *static_cast<MemoryBuffer*>(lua_touserdata(L, 1));
			lua_pushlightuserdata(L, memoryBuffer.pointer());
			return 1;
		});
		lua_setfield(L, -2, "pointer");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto& memoryBuffer = *static_cast<MemoryBuffer*>(lua_touserdata(L, 1));
			memoryBuffer.resize(luaL_checkinteger(L, 2));
			return 0;
		});
		lua_setfield(L, -2, "resize");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto& memoryBuffer = *static_cast<MemoryBuffer*>(lua_touserdata(L, 1));
			memoryBuffer.reserve(luaL_checkinteger(L, 2));
			return 0;
		});
		lua_setfield(L, -2, "reserve");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto& memoryBuffer = *static_cast<MemoryBuffer*>(lua_touserdata(L, 1));
			memoryBuffer.close();
			return 0;
		});
		lua_setfield(L, -2, "close");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto& memoryBuffer = *static_cast<MemoryBuffer*>(lua_touserdata(L, 1));
			lua_pushboolean(L, memoryBuffer.check(luaL_checkinteger(L, 2)));
			return 0;
		});
		lua_setfield(L, -2, "check");



		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto& memoryBuffer = *static_cast<MemoryBuffer*>(lua_touserdata(L, 1));
			if (!memoryBuffer.skip(luaL_checkinteger(L, 2))) {
				return luaL_error(L, "Assertion failed cursor <= size");
			}
			return 0;
		});
		lua_setfield(L, -2, "skip");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto& memoryBuffer = *static_cast<MemoryBuffer*>(lua_touserdata(L, 1));
			luaL_checktype(L, 2, LUA_TLIGHTUSERDATA);
			memoryBuffer.put<void*>(lua_touserdata(L, 2));
			return 0;
		});
		lua_setfield(L, -2, "put_ptr");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto& memoryBuffer = *static_cast<MemoryBuffer*>(lua_touserdata(L, 1));
			auto value = luaL_checkinteger(L, 2);
			memoryBuffer.put<std::uint8_t>(value);
			return 0;
		});
		lua_setfield(L, -2, "put_u8");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto& memoryBuffer = *static_cast<MemoryBuffer*>(lua_touserdata(L, 1));
			auto value = luaL_checkinteger(L, 2);
			memoryBuffer.put<std::uint16_t>(value);
			return 0;
		});
		lua_setfield(L, -2, "put_u16");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto& memoryBuffer = *static_cast<MemoryBuffer*>(lua_touserdata(L, 1));
			auto value = luaL_checkinteger(L, 2);
			memoryBuffer.put<std::uint32_t>(value);
			return 0;
		});
		lua_setfield(L, -2, "put_u32");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto& memoryBuffer = *static_cast<MemoryBuffer*>(lua_touserdata(L, 1));
			if (auto const value = memoryBuffer.get<std::uint8_t>()) { lua_pushinteger(L, *value); }
			else { luaL_error(L, "assertion failed (cursor > size): Reading further would cause an access violation"); }
			return 1;
		});
		lua_setfield(L, -2, "get_u8");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto& memoryBuffer = *static_cast<MemoryBuffer*>(lua_touserdata(L, 1));
			if (auto const value = memoryBuffer.get<std::uint16_t>()) { lua_pushinteger(L, *value); }
			else { luaL_error(L, "assertion failed (cursor > size): Reading further would cause an access violation"); }
			return 1;
		});
		lua_setfield(L, -2, "get_u16");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto& memoryBuffer = *static_cast<MemoryBuffer*>(lua_touserdata(L, 1));
			if (auto const value = memoryBuffer.get<std::uint32_t>()) { lua_pushinteger(L, *value); }
			else { luaL_error(L, "assertion failed (cursor > size): Reading further would cause an access violation"); }
			return 1;
		});
		lua_setfield(L, -2, "get_u32");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			auto& memoryBuffer = *static_cast<MemoryBuffer*>(lua_touserdata(L, 1));
			if (auto const value = memoryBuffer.get<void*>()) { lua_pushlightuserdata(L, *value); }
			else { luaL_error(L, "assertion failed (cursor > size): Reading further would cause an access violation"); }
			return 1;
		});
		lua_setfield(L, -2, "get_ptr");

		lua_setglobal(L, "MemoryBuffer");

		lua_newtable(L);
		lua_pushcfunction(L, [](lua_State *L) -> int {
			lua_pushlightuserdata(L, std::malloc(luaL_checkinteger(L, 1)));
		return 1;
		});
		lua_setfield(L, -2, "malloc");
		lua_pushcfunction(L, [](lua_State *L) -> int {
			luaL_checktype(L, 1, LUA_TLIGHTUSERDATA);
			std::free(lua_touserdata(L, 1));
			return 1;
		});
		lua_setfield(L, -2, "free");
		lua_pushcfunction(L, [](lua_State *L) -> int {
			luaL_checktype(L, 1, LUA_TLIGHTUSERDATA);
			lua_pushinteger(L, *static_cast<std::uint32_t*>(lua_touserdata(L, 1)));
			return 1;
		});
		lua_setfield(L, -2, "read_u32");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			luaL_checktype(L, 1, LUA_TLIGHTUSERDATA);
			auto* data = static_cast<std::uint8_t*>(lua_touserdata(L, 1));
			lua_pushlightuserdata(L, data + luaL_checkinteger(L, 2));
			return 1;
		});
		lua_setfield(L, -2, "offset");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			luaL_checktype(L, 1, LUA_TLIGHTUSERDATA);
			std::uint32_t& value = *static_cast<std::uint32_t*>(lua_touserdata(L, 1));
			value = luaL_checkinteger(L, 2);
			return 0;
		});
		lua_setfield(L, -2, "write_u32");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			luaL_checktype(L, 1, LUA_TLIGHTUSERDATA);
			float& value = *static_cast<float*>(lua_touserdata(L, 1));
			value = luaL_checknumber(L, 2);
			return 0;
		});
		lua_setfield(L, -2, "write_f32");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			luaL_checktype(L, 1, LUA_TLIGHTUSERDATA);
			luaL_checktype(L, 2, LUA_TLIGHTUSERDATA);
			auto* value = static_cast<std::uintptr_t*>(lua_touserdata(L, 1));
			*value = std::bit_cast<std::uintptr_t>(lua_touserdata(L, 2));
			return 0;
		});
		lua_setfield(L, -2, "write_ptr");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			luaL_checktype(L, 1, LUA_TLIGHTUSERDATA);
			void* target = lua_touserdata(L, 1);
			char const* source = luaL_checkstring(L, 2);
			int size = luaL_checkinteger(L, 3);
			memcpy(target, source, size);
			return 0;
		});
		lua_setfield(L, -2, "write_bytes");

		lua_pushcfunction(L, [](lua_State *L) -> int {
			lua_pushlightuserdata(L, nullptr);
			return 1;
		});
		lua_setfield(L, -2, "null");
		lua_setglobal(L, "Memory");
	}

	aurora::api_register_gl(L);
	lua_setglobal(L, "gl");
	aurora::api_register_gl_constants(L);
	lua_setglobal(L, "GL");
}