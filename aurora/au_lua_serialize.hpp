#pragma once

#include <string>
#include <lua.hpp>

namespace aurora {
	void lapi_dump_stack(lua_State* L);
	std::string lapi_serialize(lua_State* L);
}