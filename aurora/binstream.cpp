// This implementation is still experimental and not final, more development must be done before this is ready to use

#include <string>
#include <vector>
#include <cstdint>
#include <optional>
#include <stdexcept>

#include <lua.hpp>

#define AU_FIELD(aSerializer, aField) aSerializer.serialize(#aField, aField)

namespace aurora {
	struct Serializer;

	struct Serializable {
		virtual ~Serializable() = default;
		virtual void serialize(Serializer& a) = 0;
	};

	struct Serializer {
		virtual void serialize(char const* aField, bool& aValue) = 0;
		virtual void serialize(char const* aField, float& aValue) = 0;
		virtual void serialize(char const* aField, std::uint8_t& aValue) = 0;
		virtual void serialize(char const* aField, std::uint16_t& aValue) = 0;
		virtual void serialize(char const* aField, std::uint32_t& aValue) = 0;
		virtual void serialize(char const* aField, std::int8_t& aValue) = 0;
		virtual void serialize(char const* aField, std::int16_t& aValue) = 0;
		virtual void serialize(char const* aField, std::int32_t& aValue) = 0;
		virtual void serialize(char const* aField, std::string& aValue, bool aSizedString = true) = 0;

		virtual void serialize(char const* aField, Serializable& aValue) = 0;

		template<typename T> void serialize(char const* aField, std::vector<T>& aValue) {
			if (auto optNewSize = array_begin()) { aValue.resize(*optNewSize); }

			for (std::size_t i = 0; i < aValue.size(); ++i) {
				serialize(nullptr, aValue[i]);
				array_next(i);
			}
			array_end(aField);
		}

		// ---------------------------
		// -- Implementation Detail --
		// ---------------------------

		constexpr Serializer() noexcept = default;
		virtual ~Serializer() = default;
		Serializer(Serializer const&) = delete;
		Serializer& operator=(Serializer const&) = delete;
		Serializer(Serializer&&) noexcept = delete;
		Serializer& operator=(Serializer&&) noexcept = delete;

		virtual std::optional<std::size_t> array_begin() = 0;
		virtual void array_next(std::size_t aIndex) = 0;
		virtual void array_end(char const* aField) = 0;

		// ---------------------------
	};

	struct LuaWriterSerializer final : public Serializer {
		lua_State* L = nullptr;

		void serialize(char const* aField, bool& aValue) override {
			lua_pushboolean(L, aValue);
			if (aField) lua_setfield(L, -2, aField);
		};

		void serialize(char const* aField, float& aValue) override {
			lua_pushnumber(L, aValue);
			if (aField) lua_setfield(L, -2, aField);
		};

		void impl_integer(char const* aField, lua_Integer aValue) {
			lua_pushinteger(L, aValue);
			if (aField) lua_setfield(L, -2, aField);
		}

		void serialize(char const* aField, std::uint8_t& aValue) override { impl_integer(aField, static_cast<lua_Integer>(aValue)); };
		void serialize(char const* aField, std::uint16_t& aValue) override { impl_integer(aField, static_cast<lua_Integer>(aValue)); };
		void serialize(char const* aField, std::uint32_t& aValue) override { impl_integer(aField, static_cast<lua_Integer>(aValue)); };
		void serialize(char const* aField, std::int8_t& aValue) override { impl_integer(aField, static_cast<lua_Integer>(aValue)); };
		void serialize(char const* aField, std::int16_t& aValue) override { impl_integer(aField, static_cast<lua_Integer>(aValue)); };
		void serialize(char const* aField, std::int32_t& aValue) override { impl_integer(aField, static_cast<lua_Integer>(aValue)); };

		void serialize(char const* aField, std::string& aValue, bool aSizedString = true) override {
			lua_pushlstring(L, aValue.data(), aValue.size());
			if (aField) lua_setfield(L, -2, aField);
		};

		void serialize(char const* aField, Serializable& aValue) override {
			lua_newtable(L);
			aValue.serialize(*this);
			if (aField) lua_setfield(L, -2, aField);
		}

		std::optional<std::size_t> array_begin() override { lua_newtable(L); return std::nullopt; }
		void array_next(std::size_t aIndex) override { lua_rawseti(L, -2, aIndex + 1); }
		void array_end(char const* aField) override { lua_setfield(L, -2, aField); };
	};

	struct BinaryReaderSerializer final : public Serializer {
		virtual ~BinaryReaderSerializer() = default;

		char const* const kError = "ByteStream out of range";

		std::vector<std::byte> mBuffer;
		std::size_t mOffset = 0;

		template<typename T>
		void read_gen(T& aValue) {
			if (mOffset + sizeof(T) > mBuffer.size()) throw std::out_of_range(kError);
			std::memcpy(&aValue, mBuffer.data() + mOffset, sizeof(T));
			mOffset += sizeof(T);
		}

		void serialize(char const* aField, bool& aValue) override { read_gen(aValue); };
		void serialize(char const* aField, float& aValue) override { read_gen(aValue); };
		void serialize(char const* aField, std::uint8_t& aValue) override { read_gen(aValue); };
		void serialize(char const* aField, std::uint16_t& aValue) override { read_gen(aValue); };
		void serialize(char const* aField, std::uint32_t& aValue) override { read_gen(aValue); };
		void serialize(char const* aField, std::int8_t& aValue) override { read_gen(aValue); };
		void serialize(char const* aField, std::int16_t& aValue) override { read_gen(aValue); };
		void serialize(char const* aField, std::int32_t& aValue) override { read_gen(aValue); };

		void serialize(char const* aField, std::string& aValue, bool aSizedString = true) override {
			if (aSizedString) {
				std::uint32_t newSize;
				serialize(nullptr, newSize);
				if (mOffset + newSize > mBuffer.size()) throw std::out_of_range(kError);
				aValue = std::string(reinterpret_cast<char const*>(mBuffer.data() + mOffset), newSize);
				mOffset += newSize;
			}
			else {
				std::size_t size = std::strlen(reinterpret_cast<char const*>(mBuffer.data() + mOffset));
				std::string string = std::string(reinterpret_cast<char const*>(mBuffer.data() + mOffset), size);
				mOffset += size;
				mOffset += 1;
			}
		};

		void serialize(char const* aField, Serializable& aValue) override { aValue.serialize(*this); };

		std::optional<std::size_t> array_begin() override {
			std::uint32_t count;
			serialize(nullptr, count);
			return std::optional(static_cast<std::size_t>(count));
		};

		void array_next(std::size_t aIndex) override {};
		void array_end(char const* aField) override {};
	};
}