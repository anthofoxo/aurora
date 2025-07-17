#pragma once

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
		void process(Serializable& aValue) { serialize(nullptr, aValue); }

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

		template<typename T> inline void serialize(char const* aField, std::vector<T>& aValue) {
			auto newSize = aValue.size();
			array_begin(aField, newSize);

			if (newSize != aValue.size()) aValue.resize(newSize);

			for (std::size_t i = 0; i < aValue.size(); ++i) {
				array_iter_prologue(i);
				serialize(nullptr, aValue[i]);
				array_iter_epilogue(i);
			}

			array_end(aField);
		}

		constexpr Serializer() noexcept = default;
		virtual ~Serializer() = default;
		Serializer(Serializer const&) = delete;
		Serializer& operator=(Serializer const&) = delete;
		Serializer(Serializer&&) noexcept = delete;
		Serializer& operator=(Serializer&&) noexcept = delete;

		virtual void array_begin(char const* aField, std::size_t& aSize) = 0;
		virtual void array_iter_prologue(std::size_t aIndex) = 0;
		virtual void array_iter_epilogue(std::size_t aIndex) = 0;
		virtual void array_end(char const* aField) = 0;
	};

	struct SerializerReaderLua final : public Serializer {
		template<typename T>
		inline void impl_integer(char const* aField, T& aValue) {
			if (aField) lua_getfield(L, -1, aField);
			aValue = static_cast<T>(lua_tonumber(L, -1));
			if (aField) lua_pop(L, 1);
		}

		void serialize(char const* aField, bool& aValue) override;
		void serialize(char const* aField, float& aValue) override;
		inline void serialize(char const* aField, std::uint8_t& aValue) override { impl_integer(aField, aValue); };
		inline void serialize(char const* aField, std::uint16_t& aValue) override { impl_integer(aField, aValue); };
		inline void serialize(char const* aField, std::uint32_t& aValue) override { impl_integer(aField, aValue); };
		inline void serialize(char const* aField, std::int8_t& aValue) override { impl_integer(aField, aValue); };
		inline void serialize(char const* aField, std::int16_t& aValue) override { impl_integer(aField, aValue); };
		inline void serialize(char const* aField, std::int32_t& aValue) override { impl_integer(aField, aValue); };
		void serialize(char const* aField, std::string& aValue, bool aSizedString = true) override;
		void serialize(char const* aField, Serializable& aValue) override;
		void array_begin(char const* aField, std::size_t& aSize) override;
		inline void array_iter_prologue(std::size_t i) { lua_rawgeti(L, -1, i + 1); }
		inline void array_iter_epilogue(std::size_t aIndex) override { lua_pop(L, 1); }
		inline void array_end(char const* aField) override { lua_pop(L, 1); };

		lua_State* L = nullptr;
	};

	struct SerializerWriterLua final : public Serializer {
		void serialize(char const* aField, bool& aValue) override;
		void serialize(char const* aField, float& aValue) override;
		inline void serialize(char const* aField, std::uint8_t& aValue) override { impl_integer(aField, static_cast<lua_Integer>(aValue)); };
		inline void serialize(char const* aField, std::uint16_t& aValue) override { impl_integer(aField, static_cast<lua_Integer>(aValue)); };
		inline void serialize(char const* aField, std::uint32_t& aValue) override { impl_integer(aField, static_cast<lua_Integer>(aValue)); };
		inline void serialize(char const* aField, std::int8_t& aValue) override { impl_integer(aField, static_cast<lua_Integer>(aValue)); };
		inline void serialize(char const* aField, std::int16_t& aValue) override { impl_integer(aField, static_cast<lua_Integer>(aValue)); };
		inline void serialize(char const* aField, std::int32_t& aValue) override { impl_integer(aField, static_cast<lua_Integer>(aValue)); };
		void serialize(char const* aField, std::string& aValue, bool aSizedString = true) override;
		void serialize(char const* aField, Serializable& aValue) override;
		inline void array_begin(char const* aField, std::size_t& aSize) override { lua_newtable(L); }
		inline void array_iter_prologue(std::size_t aIndex) override {};
		inline void array_iter_epilogue(std::size_t aIndex) override { lua_rawseti(L, -2, aIndex + 1); }
		inline void array_end(char const* aField) override { if (aField) lua_setfield(L, -2, aField); };

		void impl_integer(char const* aField, lua_Integer aValue);
		virtual ~SerializerWriterLua() noexcept = default;
		lua_State* L = nullptr;
	};

	struct SerializerWriterBinary final : public Serializer {
		template<typename T>
		void write_gen(T const& value) {
			for (int i = 0; i < sizeof(T); ++i) mBuffer.emplace_back(static_cast<std::byte>(0));
			std::memcpy(mBuffer.data() + mBuffer.size() - sizeof(T), &value, sizeof(T));
		}

		void serialize(char const* aField, bool& aValue) override { write_gen(aValue); };
		void serialize(char const* aField, float& aValue) override { write_gen(aValue); };
		void serialize(char const* aField, std::uint8_t& aValue) override { write_gen(aValue); };
		void serialize(char const* aField, std::uint16_t& aValue) override { write_gen(aValue); };
		void serialize(char const* aField, std::uint32_t& aValue) override { write_gen(aValue); };
		void serialize(char const* aField, std::int8_t& aValue) override { write_gen(aValue); };
		void serialize(char const* aField, std::int16_t& aValue) override { write_gen(aValue); };
		void serialize(char const* aField, std::int32_t& aValue) override { write_gen(aValue); };
		void serialize(char const* aField, std::string& aValue, bool aSizedString = true) override;
		void serialize(char const* aField, Serializable& aValue) override { aValue.serialize(*this); };
		void array_begin(char const* aField, std::size_t& aSize) override;
		void array_iter_prologue(std::size_t aIndex) override {};
		void array_iter_epilogue(std::size_t aIndex) override {};
		void array_end(char const* aField) override {};

		virtual ~SerializerWriterBinary() noexcept = default;
		std::vector<std::byte> mBuffer;
	};

	struct SerializerReaderBinary final : public Serializer {
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
		void serialize(char const* aField, std::string& aValue, bool aSizedString = true) override;
		void serialize(char const* aField, Serializable& aValue) override { aValue.serialize(*this); };
		void array_begin(char const* aField, std::size_t& aSize) override;
		void array_iter_prologue(std::size_t aIndex) override {};
		void array_iter_epilogue(std::size_t aIndex) override {};
		void array_end(char const* aField) override {};

		virtual ~SerializerReaderBinary() noexcept = default;
		char const* const kError = "ByteStream out of range";
		std::vector<std::byte> mBuffer;
		std::size_t mOffset = 0;
	};
}