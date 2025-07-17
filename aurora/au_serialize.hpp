#pragma once

#include <vector>
#include <cstddef>
#include <cstdint>
#include <cstdlib>
#include <stdexcept>
#include <string>
#include <span>

namespace aurora {
	struct ByteStream final {
		[[deprecated]] ByteStream() = default;

		char const* const kError = "ByteStream out of range";

		std::vector<std::byte> mBuffer;
		std::size_t mOffset = 0;

		template<typename T>
		T read_gen() {
			if (mOffset + sizeof(T) > mBuffer.size()) throw std::out_of_range(kError);
			T value;
			std::memcpy(&value, mBuffer.data() + mOffset, sizeof(T));
			mOffset += sizeof(T);
			return value;
		}

		bool read_bool() { return read_gen<std::uint8_t>(); }
		std::uint32_t read_u8() { return read_gen<std::uint8_t>(); }
		std::uint32_t read_u32() { return read_gen<std::uint32_t>(); }

		float read_f32() { return read_gen<float>(); }

		std::string read_sstr() {
			auto size = read_u32();
			if (mOffset + size > mBuffer.size()) throw std::out_of_range(kError);
			std::string string = std::string(reinterpret_cast<char const*>(mBuffer.data() + mOffset), size);
			mOffset += size;
			return string;
		}

		std::string read_cstr() {
			// We cannot guarentee safety with a c string
			std::size_t size = std::strlen(reinterpret_cast<char const*>(mBuffer.data() + mOffset));
			std::string string = std::string(reinterpret_cast<char const*>(mBuffer.data() + mOffset), size);
			mOffset += size;
			mOffset += 1; // Null terminator
			return string;
		}

		template<typename T>
		void write_gen(T const& value) {
			for (int i = 0; i < sizeof(T); ++i) mBuffer.emplace_back(static_cast<std::byte>(0));
			std::memcpy(mBuffer.data() + mBuffer.size() - sizeof(T), &value, sizeof(T));
		}

		void write_bool(bool value) { return write_gen(value); }
		void write_u8(std::uint8_t value) { return write_gen(value); }
		void write_u32(std::uint32_t value) { return write_gen(value); }

		void write_sstr(std::string const& value) {
			write_u32(static_cast<std::uint32_t>(value.size()));
			for (auto c : value) mBuffer.emplace_back(static_cast<std::byte>(c));
		}

		void write_cstr(std::string const& value) {
			for (auto c : value) mBuffer.emplace_back(static_cast<std::byte>(c));
			mBuffer.emplace_back(static_cast<std::byte>('\0'));
		}
	};
}