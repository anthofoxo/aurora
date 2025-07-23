#pragma once

#include <lua.hpp>
#include <string>

namespace aurora {
void lapi_dump_stack(lua_State* L);
std::string lapi_serialize(lua_State* L);
}  // namespace aurora