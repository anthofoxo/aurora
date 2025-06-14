#pragma once

#include <lua.hpp>

#define AU_IMPL_API_REGISTER(name) lua_pushcfunction(L, &name); lua_setfield(L, -2, #name);
namespace aurora {
    void api_register_imgui(lua_State *L);
    void api_register_filesystem(lua_State *L);
    void api_register_glm(lua_State *L);
}
