#include "au_api.hpp"

#include <filesystem>

namespace {
    int create_directories(lua_State *L) {
        lua_pushboolean(L, std::filesystem::create_directories(luaL_checkstring(L, 1)));
        return 1;
    }

    int create_directory(lua_State *L) {
        lua_pushboolean(L, std::filesystem::create_directory(luaL_checkstring(L, 1)));
        return 1;
    }

    int directory_iterator(lua_State *L) {
        char const* directory = luaL_checkstring(L, 1);

        if (!std::filesystem::exists(directory)) {
            lua_pushnil(L);
        }
        else {
            lua_newtable(L);
            lua_Integer index = 1;

            for(auto const& entry : std::filesystem::directory_iterator(directory)) {
                std::u8string const string = entry.path().generic_u8string();
                lua_pushlstring(L, reinterpret_cast<char const*>(string.data()), std::distance(string.begin(), string.end()));
                lua_rawseti(L, -2, index++);
            }
        }

        return 1;
    }

    int exists(lua_State *L) {
        lua_pushboolean(L, std::filesystem::exists(luaL_checkstring(L, 1)));
        return 1;
    }

    int is_regular_file(lua_State *L) {
        lua_pushboolean(L, std::filesystem::is_regular_file(luaL_checkstring(L, 1)));
        return 1;
    }

    int stem(lua_State *L) {
        std::string const stem = std::filesystem::path(luaL_checkstring(L, 1)).stem().string();
        lua_pushstring(L, stem.c_str());
        return 1;
    }

}

void aurora::api_register_filesystem(lua_State *L) {
    lua_newtable(L);
    AU_IMPL_API_REGISTER(create_directories);
    AU_IMPL_API_REGISTER(create_directory);
    AU_IMPL_API_REGISTER(directory_iterator);
    AU_IMPL_API_REGISTER(exists);
    AU_IMPL_API_REGISTER(is_regular_file);
    AU_IMPL_API_REGISTER(stem);
}