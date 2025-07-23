#include "au_serial.hpp"

namespace aurora {
void SerializerReaderLua::serialize(char const* aField, bool& aValue) {
	if (aField) lua_getfield(L, -1, aField);
	aValue = static_cast<std::remove_reference_t<decltype(aValue)>>(lua_toboolean(L, -1));
	if (aField) lua_pop(L, 1);
};

void SerializerReaderLua::serialize(char const* aField, float& aValue) {
	if (aField) lua_getfield(L, -1, aField);
	aValue = static_cast<std::remove_reference_t<decltype(aValue)>>(lua_tonumber(L, -1));
	if (aField) lua_pop(L, 1);
};

void SerializerReaderLua::serialize(char const* aField, std::string& aValue, bool aSizedString) {
	if (aField) lua_getfield(L, -1, aField);
	std::size_t size;
	char const* data = lua_tolstring(L, -1, &size);
	aValue = std::string(data, size);
	if (aField) lua_pop(L, 1);
};

void SerializerReaderLua::serialize(char const* aField, Serializable& aValue) {
	if (aField) lua_getfield(L, -1, aField);
	aValue.serialize(*this);
	if (aField) lua_pop(L, 1);
}

void SerializerReaderLua::array_begin(char const* aField, std::size_t& aSize) {
	if (aField) lua_getfield(L, -1, aField);
	aSize = lua_rawlen(L, -1);
}

void SerializerWriterLua::serialize(char const* aField, bool& aValue) {
	lua_pushboolean(L, aValue);
	if (aField) lua_setfield(L, -2, aField);
};

void SerializerWriterLua::serialize(char const* aField, float& aValue) {
	lua_pushnumber(L, aValue);
	if (aField) lua_setfield(L, -2, aField);
};

void SerializerWriterLua::serialize(char const* aField, std::string& aValue, bool aSizedString) {
	lua_pushlstring(L, aValue.data(), aValue.size());
	if (aField) lua_setfield(L, -2, aField);
};

void SerializerWriterLua::serialize(char const* aField, Serializable& aValue) {
	lua_newtable(L);
	aValue.serialize(*this);
	if (aField) lua_setfield(L, -2, aField);
}

void SerializerWriterLua::impl_integer(char const* aField, lua_Integer aValue) {
	lua_pushinteger(L, aValue);
	if (aField) lua_setfield(L, -2, aField);
}

void SerializerWriterBinary::serialize(char const* aField, std::string& aValue, bool aSizedString) {
	if (aSizedString) {
		std::uint32_t size = static_cast<std::uint32_t>(aValue.size());
		serialize(nullptr, size);
		for (auto c : aValue) mBuffer.emplace_back(static_cast<std::byte>(c));
	} else {
		for (auto c : aValue) mBuffer.emplace_back(static_cast<std::byte>(c));
		mBuffer.emplace_back(static_cast<std::byte>('\0'));
	}
};

void SerializerWriterBinary::array_begin(char const* aField, std::size_t& aSize) {
	std::uint32_t count = static_cast<std::uint32_t>(aSize);
	serialize(nullptr, count);
};

void SerializerReaderBinary::serialize(char const* aField, std::string& aValue, bool aSizedString) {
	if (aSizedString) {
		std::uint32_t newSize;
		serialize(nullptr, newSize);
		if (mOffset + newSize > mBuffer.size()) throw std::out_of_range(kError);
		aValue = std::string(reinterpret_cast<char const*>(mBuffer.data() + mOffset), newSize);
		mOffset += newSize;
	} else {
		std::size_t size = std::strlen(reinterpret_cast<char const*>(mBuffer.data() + mOffset));
		std::string string = std::string(reinterpret_cast<char const*>(mBuffer.data() + mOffset), size);
		mOffset += size + 1;
	}
};

void SerializerReaderBinary::array_begin(char const* aField, std::size_t& aSize) {
	std::uint32_t count;
	serialize(nullptr, count);
	aSize = static_cast<std::size_t>(count);
};
}  // namespace aurora