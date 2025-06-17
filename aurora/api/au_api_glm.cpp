#include "au_api.hpp"

#include <glm/glm.hpp>
#include <glm/gtc/matrix_transform.hpp>

namespace {
    int perspective(lua_State *L) {
        auto const fovy = static_cast<float>(luaL_checknumber(L, 1));
        auto const aspect = static_cast<float>(luaL_checknumber(L, 2));
        auto const near = static_cast<float>(luaL_checknumber(L, 3));
        auto const far = static_cast<float>(luaL_checknumber(L, 4));
        glm::mat4 matrix = glm::perspective(fovy, aspect, near, far);
        lua_newtable(L);

        for (int j = 0; j < 4; ++j) {
            for (int i = 0; i < 4; ++i) {
                int const idx = j * 4 + i;
                lua_pushinteger(L, idx + 1);
                lua_pushnumber(L, matrix[j][i]);
                lua_settable(L, -3);
            }
        }

        return 1;
    }

    int lookAt(lua_State *L) {
        glm::vec3 eye, center, up;

        for (int i = 0; i < 3; ++i) {
            lua_rawgeti(L, 1, i + 1);
            eye[i] = lua_tonumber(L, -1);
            lua_pop(L, 1);
        }

        for (int i = 0; i < 3; ++i) {
            lua_rawgeti(L, 2, i + 1);
            center[i] = lua_tonumber(L, -1);
            lua_pop(L, 1);
        }

        for (int i = 0; i < 3; ++i) {
            lua_rawgeti(L, 3, i + 1);
            up[i] = lua_tonumber(L, -1);
            lua_pop(L, 1);
        }

        glm::mat4 matrix = glm::lookAt(eye, center, up);
        lua_newtable(L);

        for (int j = 0; j < 4; ++j) {
            for (int i = 0; i < 4; ++i) {
                int const idx = j * 4 + i;
                lua_pushinteger(L, idx + 1);
                lua_pushnumber(L, matrix[j][i]);
                lua_settable(L, -3);
            }
        }

        return 1;
    }
}

void aurora::api_register_glm(lua_State *L) {
    lua_newtable(L);
    AU_IMPL_API_REGISTER(perspective);
    AU_IMPL_API_REGISTER(lookAt);
}