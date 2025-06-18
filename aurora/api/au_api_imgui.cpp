#include "au_api.hpp"

#include <imgui.h>
#include <misc/cpp/imgui_stdlib.h>

#include <cstdint>
#include <string>

namespace {
    int BeginChild(lua_State *L) {
        lua_pushboolean(L, ImGui::BeginChild(luaL_checkstring(L, 1)));
        return 1;
    }

    int BeginMenu(lua_State *L) {
        lua_pushboolean(L, ImGui::BeginMenu(luaL_checkstring(L, 1)));
        return 1;
    }

    int BeginPopupContextItem(lua_State *L) {
        lua_pushboolean(L, ImGui::BeginPopupContextItem());
        return 1;
    }

    int BeginTable(lua_State *L) {
        char const *id = luaL_checkstring(L, 1);
        int const numCols = static_cast<int>(luaL_checkinteger(L, 2));
        lua_pushboolean(L, ImGui::BeginTable(id, numCols));
        return 1;
    }

    int BulletText(lua_State *L) {
        int const numArgs = lua_gettop(L);
        lua_getglobal(L, "string");
        lua_getfield(L, -1, "format");
        lua_remove(L, -2);
        lua_insert(L, 1);
        lua_pcall(L, numArgs, 1, 0);
        ImGui::BulletText("%s", lua_tostring(L, -1));
        return 0;
    }

    int Button(lua_State *L) {
        lua_pushboolean(L, ImGui::Button(luaL_checkstring(L, 1)));
        return 1;
    }

    int CloseCurrentPopup(lua_State *L) {
        ImGui::CloseCurrentPopup();
        return 0;
    }

    int Columns(lua_State *L) {
        ImGui::Columns(static_cast<int>(luaL_checkinteger(L, 1)));
        return 0;
    }

    int EndChild(lua_State *L) {
        ImGui::EndChild();
        return 0;
    }

    int EndMenu(lua_State *L) {
        ImGui::EndMenu();
        return 0;
    }

    int EndPopup(lua_State *L) {
        ImGui::EndPopup();
        return 0;
    }

    int EndTable(lua_State *L) {
        ImGui::EndTable();
        return 0;
    }

    int GetContentRegionAvail(lua_State *L) {
        auto const [x, y] = ImGui::GetContentRegionAvail();
        lua_newtable(L);
        lua_pushnumber(L, x);
        lua_rawseti(L, -2, 1);
        lua_pushnumber(L, y);
        lua_rawseti(L, -2, 2);
        return 1;
    }

    int Image(lua_State *L) {
        auto const image = static_cast<ImTextureID>(static_cast<std::uintptr_t>(luaL_checkinteger(L, 1)));

        lua_rawgeti(L, 2, 1);
        float const width = lua_tonumber(L, -1);
        lua_pop(L, 1);

        lua_rawgeti(L, 2, 2);
        float const height = lua_tonumber(L, -1);
        lua_pop(L, 1);

        ImVec2 uv0 = { 0.0f, 0.0f };
        ImVec2 uv1 = { 1.0f, 1.0f };

        if (lua_gettop(L) >= 3) {
            lua_pushinteger(L, 1);
            lua_rawget(L, 3);
            uv0.x = lua_tonumber(L, -1);
            lua_pop(L, 1);
            lua_pushinteger(L, 2);
            lua_rawget(L, 3);
            uv0.y = lua_tonumber(L, -1);
            lua_pop(L, 1);
        }

        if (lua_gettop(L) >= 4) {
            lua_pushinteger(L, 1);
            lua_rawget(L, 4);
            uv1.x = lua_tonumber(L, -1);
            lua_pop(L, 1);
            lua_pushinteger(L, 2);
            lua_rawget(L, 4);
            uv1.y = lua_tonumber(L, -1);
            lua_pop(L, 1);
        }

        ImGui::Image(image, {width, height }, uv0, uv1);

        return 0;
    }

    int InputText(lua_State *L) {
        lua_rawgeti(L, 2, 1);
        std::string text = lua_tostring(L, -1);
        lua_pop(L, 1);
        bool const result = ImGui::InputText(luaL_checkstring(L, 1), &text);
        lua_pushstring(L, text.c_str());
        lua_rawseti(L, 2, 1);

        lua_pushboolean(L, result);
        return 1;
    }

    int LabelText(lua_State *L) {
        char const* label = luaL_checkstring(L, 1);
        int const numArgs = lua_gettop(L);
        lua_getglobal(L, "string");
        lua_getfield(L, -1, "format");
        lua_remove(L, -2);
        lua_insert(L, 2);
        lua_pcall(L, numArgs - 1, 1, 0);
        ImGui::LabelText(label, "%s", lua_tostring(L, -1));
        return 0;
    }

    int LogFinish(lua_State *L) {
        ImGui::LogFinish();
        return 0;
    }

    int LogText(lua_State *L) {
        int const numArgs = lua_gettop(L);
        lua_getglobal(L, "string");
        lua_getfield(L, -1, "format");
        lua_remove(L, -2);
        lua_insert(L, 1);
        lua_pcall(L, numArgs, 1, 0);
        ImGui::LogText("%s", lua_tostring(L, -1));
        return 0;
    }

    int LogToClipboard(lua_State *L) {
        ImGui::LogToClipboard();
        return 0;
    }

    int MenuItem(lua_State *L) {
        char const* label = luaL_checkstring(L, 1);

        bool value = ImGui::MenuItem(label);

        lua_pushboolean(L, value);
        return 1;
    }

    int NextColumn(lua_State *L) {
        ImGui::NextColumn();
        return 0;
    }

    int PopStyleVar(lua_State *L) {
        ImGui::PopStyleVar();
        return 0;
    }

    int PushStyleVar(lua_State *L) {
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

    int SameLine(lua_State *L) {
        ImGui::SameLine();
        return 0;
    }

    int Selectable(lua_State *L) {
        char const *label = luaL_checkstring(L, 1);

        bool selected = false;
        if (lua_gettop(L) == 2) { selected = lua_toboolean(L, 2); }

        lua_pushboolean(L, ImGui::Selectable(label, selected));
        return 1;
    }

    int Separator(lua_State *L) {
        ImGui::Separator();
        return 0;
    }

    int SeparatorText(lua_State *L) {
        ImGui::SeparatorText(luaL_checkstring(L, 1));
        return 0;
    }

    int SetItemTooltip(lua_State *L) {
        ImGui::SetItemTooltip(luaL_checkstring(L, 1));
        return 0;
    }

    int SmallButton(lua_State *L) {
        lua_pushboolean(L, ImGui::SmallButton(luaL_checkstring(L, 1)));
        return 1;
    }

    int TableNextRow(lua_State *L) {
        ImGui::TableNextRow();
        return 0;
    }

    int TableSetColumnIndex(lua_State *L) {
        ImGui::TableSetColumnIndex(static_cast<int>(luaL_checkinteger(L, 1)));
        return 0;
    }

    int Text(lua_State *L) {
        int const numArgs = lua_gettop(L);
        lua_getglobal(L, "string");
        lua_getfield(L, -1, "format");
        lua_remove(L, -2);
        lua_insert(L, 1);
        lua_pcall(L, numArgs, 1, 0);
        ImGui::TextUnformatted(lua_tostring(L, -1));
        return 0;
    }

    int TextColored(lua_State *L) {
        lua_rawgeti(L, 1, 1);
        float const r = lua_tonumber(L, -1);
        lua_pop(L, 1);

        lua_rawgeti(L, 1, 2);
        float const g = lua_tonumber(L, -1);
        lua_pop(L, 1);

        lua_rawgeti(L, 1, 3);
        float const b = lua_tonumber(L, -1);
        lua_pop(L, 1);

        lua_rawgeti(L, 1, 4);
        float const a = lua_tonumber(L, -1);
        lua_pop(L, 1);

        int const numArgs = lua_gettop(L);
        lua_getglobal(L, "string");
        lua_getfield(L, -1, "format");
        lua_remove(L, -2);
        lua_insert(L, 2);
        lua_pcall(L, numArgs - 1, 1, 0);

        ImGui::TextColored({ r, g, b, a }, "%s", lua_tostring(L, -1));

        return 0;
    }

    int TextUnformatted(lua_State *L) {
        ImGui::TextUnformatted(luaL_checkstring(L, 1));
        return 0;
    }

    int TextWrapped(lua_State *L) {
        int const numArgs = lua_gettop(L);
        lua_getglobal(L, "string");
        lua_getfield(L, -1, "format");
        lua_remove(L, -2);
        lua_insert(L, 1);
        lua_pcall(L, numArgs, 1, 0);
        ImGui::TextWrapped("%s", lua_tostring(L, -1));
        return 0;
    }

    int TreeNode(lua_State *L) {
        lua_pushboolean(L, ImGui::TreeNode(luaL_checkstring(L, 1)));
        return 1;
    }

    int TreeNodeEx(lua_State *L) {
        lua_pushboolean(L, ImGui::TreeNodeEx(luaL_checkstring(L, 1), luaL_checkinteger(L, 2)));
        return 1;
    }

    int TreePop(lua_State *L) {
        ImGui::TreePop();
        return 0;
    }

    int IsItemActivated(lua_State *L) {
        lua_pushboolean(L, ImGui::IsItemActivated());
        return 1;
    }
}

void aurora::api_register_imgui(lua_State *L) {
    lua_newtable(L);
    AU_IMPL_API_REGISTER(BeginChild);
    AU_IMPL_API_REGISTER(BeginMenu);
    AU_IMPL_API_REGISTER(BeginTable);
    AU_IMPL_API_REGISTER(BulletText);
    AU_IMPL_API_REGISTER(Button);
    AU_IMPL_API_REGISTER(Columns);
    AU_IMPL_API_REGISTER(EndChild);
    AU_IMPL_API_REGISTER(EndMenu);
    AU_IMPL_API_REGISTER(EndTable);
    AU_IMPL_API_REGISTER(GetContentRegionAvail);
    AU_IMPL_API_REGISTER(Image);
    AU_IMPL_API_REGISTER(InputText);
    AU_IMPL_API_REGISTER(LabelText);
    AU_IMPL_API_REGISTER(LogFinish);
    AU_IMPL_API_REGISTER(LogText);
    AU_IMPL_API_REGISTER(LogToClipboard);
    AU_IMPL_API_REGISTER(MenuItem);
    AU_IMPL_API_REGISTER(NextColumn);
    AU_IMPL_API_REGISTER(PopStyleVar);
    AU_IMPL_API_REGISTER(PushStyleVar);
    AU_IMPL_API_REGISTER(SameLine);
    AU_IMPL_API_REGISTER(Selectable);
    AU_IMPL_API_REGISTER(Separator);
    AU_IMPL_API_REGISTER(SeparatorText);
    AU_IMPL_API_REGISTER(SmallButton);
    AU_IMPL_API_REGISTER(TableNextRow);
    AU_IMPL_API_REGISTER(TableSetColumnIndex);
    AU_IMPL_API_REGISTER(Text);
    AU_IMPL_API_REGISTER(TextColored);
    AU_IMPL_API_REGISTER(TextUnformatted);
    AU_IMPL_API_REGISTER(TextWrapped);
    AU_IMPL_API_REGISTER(BeginPopupContextItem);
    AU_IMPL_API_REGISTER(EndPopup);
    AU_IMPL_API_REGISTER(CloseCurrentPopup);
    AU_IMPL_API_REGISTER(SetItemTooltip);
    AU_IMPL_API_REGISTER(TreeNode);
    AU_IMPL_API_REGISTER(TreeNodeEx);
    AU_IMPL_API_REGISTER(TreePop);
    AU_IMPL_API_REGISTER(IsItemActivated);
}