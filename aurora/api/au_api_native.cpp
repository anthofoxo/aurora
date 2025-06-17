#include <cstdint>

#include "au_api.hpp"

#include <cstdlib>

namespace {
    int malloc(lua_State *L) {
        void *ptr = std::malloc(luaL_checkinteger(L, 1));
        lua_pushlightuserdata(L, ptr);
        return 1;
    }

    int calloc(lua_State *L) {
        void* ptr = std::calloc(luaL_checkinteger(L, 1), luaL_checkinteger(L, 2));
        lua_pushlightuserdata(L, ptr);
        return 1;
    }

    int realloc(lua_State *L) {
        luaL_checktype(L, 1, LUA_TLIGHTUSERDATA);
        void* ptr = std::realloc(lua_touserdata(L, 1), luaL_checkinteger(L, 2));
        lua_pushlightuserdata(L, ptr);
        return 1;
    }

    int free(lua_State *L) {
        luaL_checktype(L, 1, LUA_TLIGHTUSERDATA);
        std::free(lua_touserdata(L, 1));
        return 0;
    }

    int offset(lua_State *L) {
        luaL_checktype(L, 1, LUA_TLIGHTUSERDATA);
        void* ptr = static_cast<std::uint8_t*>(lua_touserdata(L, 1)) + luaL_checkinteger(L, 2);
        lua_pushlightuserdata(L, ptr);
        return 1;
    }

    template<typename T, typename CastTo, auto PushFunc>
    int read_generic(lua_State *L) {
        luaL_checktype(L, 1, LUA_TLIGHTUSERDATA);
        auto const *ptr = static_cast<char*>(lua_touserdata(L, 1));
        ptr += luaL_checkinteger(L, 2);
        PushFunc(L, static_cast<CastTo>(*reinterpret_cast<T*>(ptr)));
        return 1;
    }

    template<typename T, typename CastTo, auto PullFunc>
    int write_generic(lua_State *L) {
        luaL_checktype(L, 1, LUA_TLIGHTUSERDATA);
        auto const *ptr = static_cast<char*>(lua_touserdata(L, 1));
        ptr += luaL_checkinteger(L, 2);
        *reinterpret_cast<T*>(ptr) = static_cast<CastTo>(PullFunc(L, 3));
        return 0;
    }

    template<typename T> int read_generic_integer(lua_State *L) { return read_generic<T, lua_Integer, lua_pushinteger>(L); }

    int read_u8(lua_State *L) { return read_generic_integer<std::uint8_t>(L); }
    int read_u16(lua_State *L) { return read_generic_integer<std::uint16_t>(L); }
    int read_u32(lua_State *L) { return read_generic_integer<std::uint32_t>(L); }
    int read_s8(lua_State *L) { return read_generic_integer<std::int8_t>(L); }
    int read_s16(lua_State *L) { return read_generic_integer<std::int16_t>(L); }
    int read_s32(lua_State *L) { return read_generic_integer<std::int32_t>(L); }
    int read_f32(lua_State *L) { return read_generic<float, lua_Number, lua_pushnumber>(L); }
    int read_ptr(lua_State *L) { return read_generic<void*, void*, lua_pushlightuserdata>(L); }

    template<typename T> int write_generic_integer(lua_State *L) { return write_generic<T, lua_Integer, luaL_checkinteger>(L); }

    int write_u8(lua_State *L) { return write_generic_integer<std::uint8_t>(L); }
    int write_u16(lua_State *L) { return write_generic_integer<std::uint16_t>(L); }
    int write_u32(lua_State *L) { return write_generic_integer<std::uint32_t>(L); }
    int write_s8(lua_State *L) { return write_generic_integer<std::int8_t>(L); }
    int write_s16(lua_State *L) { return write_generic_integer<std::int16_t>(L); }
    int write_s32(lua_State *L) { return write_generic_integer<std::int32_t>(L); }
    int write_f32(lua_State *L) { return write_generic<float, lua_Number, luaL_checknumber>(L); }
    int write_ptr(lua_State *L) { luaL_checktype(L, 3, LUA_TLIGHTUSERDATA); return write_generic<void*, void*, lua_touserdata>(L); }
}

void aurora::api_register_native(lua_State *L) {
    lua_newtable(L);
    AU_IMPL_API_REGISTER(malloc);
    AU_IMPL_API_REGISTER(calloc);
    AU_IMPL_API_REGISTER(realloc);
    AU_IMPL_API_REGISTER(free);
    AU_IMPL_API_REGISTER(offset);

    AU_IMPL_API_REGISTER(read_u8);
    AU_IMPL_API_REGISTER(read_u16);
    AU_IMPL_API_REGISTER(read_u32);
    AU_IMPL_API_REGISTER(read_s8);
    AU_IMPL_API_REGISTER(read_s16);
    AU_IMPL_API_REGISTER(read_s32);
    AU_IMPL_API_REGISTER(read_f32);
    AU_IMPL_API_REGISTER(read_ptr);

    AU_IMPL_API_REGISTER(write_u8);
    AU_IMPL_API_REGISTER(write_u16);
    AU_IMPL_API_REGISTER(write_u32);
    AU_IMPL_API_REGISTER(write_s8);
    AU_IMPL_API_REGISTER(write_s16);
    AU_IMPL_API_REGISTER(write_s32);
    AU_IMPL_API_REGISTER(write_f32);
    AU_IMPL_API_REGISTER(write_ptr);
}