#include "au_lua_serialize.hpp"

namespace aurora {
namespace {
	bool is_valid_lua_identifier(const std::string& s) {
		if (s.empty()) return false;
		if (!(std::isalpha(static_cast<unsigned char>(s[0])) || s[0] == '_')) return false;
		for (char c : s) {
			if (!(std::isalnum(static_cast<unsigned char>(c)) || c == '_')) return false;
		}
		return true;
	}

	void add_indent(std::string& out, int indent_level, int indent_size = 2) { out.append(indent_level * indent_size, ' '); }

	void serialize_value(lua_State* L, int index, std::string& out, int indent_level);

	bool is_array(lua_State* L, int index) {
		index = lua_absindex(L, index);
		size_t len = lua_rawlen(L, index);
		int actual_count = 0;

		lua_pushnil(L);
		while (lua_next(L, index)) {
			if (lua_type(L, -2) != LUA_TNUMBER || !lua_isinteger(L, -2)) {
				lua_pop(L, 2);
				return false;
			}
			lua_Integer key = lua_tointeger(L, -2);
			if (key < 1 || static_cast<size_t>(key) > len) {
				lua_pop(L, 2);
				return false;
			}
			actual_count++;
			lua_pop(L, 1);  // pop value, keep key
		}

		return static_cast<size_t>(actual_count) == len;
	}

	void serialize_table(lua_State* L, int index, std::string& out, int indent_level) {
		index = lua_absindex(L, index);
		bool array = is_array(L, index);
		out += "{\n";
		bool first = true;

		if (array) {
			size_t len = lua_rawlen(L, index);
			for (size_t i = 1; i <= len; ++i) {
				if (!first) out += ",\n";
				first = false;

				add_indent(out, indent_level + 1);
				lua_rawgeti(L, index, static_cast<lua_Integer>(i));
				serialize_value(L, -1, out, indent_level + 1);
				lua_pop(L, 1);  // pop value
			}
		} else {
			lua_pushnil(L);  // first key
			while (lua_next(L, index)) {
				if (!first) out += ",\n";
				first = false;

				add_indent(out, indent_level + 1);

				int key_type = lua_type(L, -2);
				if (key_type == LUA_TSTRING) {
					std::string key = lua_tostring(L, -2);
					if (is_valid_lua_identifier(key)) {
						out += key + " = ";
					} else {
						out += "[\"" + key + "\"] = ";
					}
				} else if (key_type == LUA_TNUMBER) {
					out += "[";
					if (lua_isinteger(L, -2)) {
						out += std::to_string(lua_tointeger(L, -2));
					} else {
						out += std::to_string(lua_tonumber(L, -2));
					}
					out += "] = ";
				} else {
					out += "[\"<unsupported key type>\"] = ";
				}

				serialize_value(L, -1, out, indent_level + 1);
				lua_pop(L, 1);  // pop value, keep key
			}
		}

		out += "\n";
		add_indent(out, indent_level);
		out += "}";
	}

	std::string escape_lua_string(const std::string& s) {
		std::string out = "\"";
		for (char c : s) {
			switch (c) {
				case '\\':
					out += "\\\\";
					break;
				case '\"':
					out += "\\\"";
					break;
				case '\n':
					out += "\\n";
					break;
				case '\t':
					out += "\\t";
					break;
				case '\r':
					out += "\\r";
					break;
				default:
					out += c;
			}
		}
		out += "\"";
		return out;
	}

	void serialize_value(lua_State* L, int index, std::string& out, int indent_level) {
		index = lua_absindex(L, index);
		switch (lua_type(L, index)) {
			case LUA_TNIL:
				out += "nil";
				break;
			case LUA_TBOOLEAN:
				out += lua_toboolean(L, index) ? "true" : "false";
				break;
			case LUA_TNUMBER:
				if (lua_isinteger(L, index)) {
					out += std::to_string(lua_tointeger(L, index));
				} else {
					out += std::to_string(lua_tonumber(L, index));
				}
				break;
			case LUA_TSTRING:
				out += escape_lua_string(lua_tostring(L, index));
				break;
			case LUA_TTABLE:
				serialize_table(L, index, out, indent_level);
				break;
			default:
				out += "\"<unsupported type>\"";
				break;
		}
	}
}  // namespace

std::string lapi_serialize(lua_State* L) {
	std::string result;
	serialize_value(L, -1, result, 0);
	return result;
}

void lapi_dump_stack(lua_State* L) {
	int top = lua_gettop(L);
	for (int i = 1; i <= top; i++) {
		printf("%d\t%s\t", i, luaL_typename(L, i));
		switch (lua_type(L, i)) {
			case LUA_TNUMBER:
				printf("%g\n", lua_tonumber(L, i));
				break;
			case LUA_TSTRING:
				printf("%s\n", lua_tostring(L, i));
				break;
			case LUA_TBOOLEAN:
				printf("%s\n", (lua_toboolean(L, i) ? "true" : "false"));
				break;
			case LUA_TNIL:
				printf("%s\n", "nil");
				break;
			default:
				printf("%p\n", lua_topointer(L, i));
				break;
		}
	}
}
}  // namespace aurora