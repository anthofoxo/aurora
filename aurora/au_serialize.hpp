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

		std::uint32_t read_f32() { return read_gen<float>(); }

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

#if 0
struct ByteStreami {
	std::vector<std::byte> mBuffer;
	std::size_t mOffset = 0;
	bool mReadMode = true;

	void skip(std::size_t numBytes) { mOffset += numBytes; }

	template<typename T>
	void generic(T& value) {
		if (mReadMode) {
			if (mOffset + sizeof(T) > mBuffer.size()) throw std::out_of_range("ByteStream out of range");
			T value;
			std::memcpy(&value, mBuffer.data() + mOffset, sizeof(T));
			skip(sizeof(T));
			return value;
		}
		else {
			for (int i = 0; i < sizeof(T); ++i) mBuffer.emplace_back(static_cast<std::byte>(0));
			std::memcpy(mBuffer.data() + mBuffer.size() - sizeof(T), &value, sizeof(T));
		}
	}

	template<typename T, std::enable_if_t<sizeof(T) == 1, int> = 0>
	void rw_bytes(std::span<T> bytes) {
		if (mReadMode) {
			if (mOffset + bytes.size_bytes() > mBuffer.size()) throw std::out_of_range("ByteStream out of range");
			std::memcpy(bytes.data(), mBuffer.data() + mOffset, bytes.size_bytes());
			skip(bytes.size_bytes());
		}
		else {
			for (auto const& byte : bytes) {
				mBuffer.emplace_back(byte);
			}
		}
	}

	void rw_bool(bool& value) { generic(value); }
	void rw_u8(std::uint8_t& value) { generic(value); }
	void rw_u16(std::uint8_t& value) { generic(value); }
	void rw_u32(std::uint32_t& value) { generic(value); }
	void rw_u32(std::uint32_t& value) { generic(value); }
	void rw_s8(std::int8_t& value) { generic(value); }
	void rw_s16(std::int8_t& value) { generic(value); }
	void rw_s32(std::int32_t& value) { generic(value); }

	void rw_sstr(std::string& value) {
		auto size = static_cast<std::uint32_t>(value.size());
		rw_u32(size);
		if (mReadMode) { value.resize(size); }
		rw_bytes<char>(value);
	}

	void rw_cstr(std::string& value) {
		if (mReadMode) {
			std::strlen(reinterpret_cast<char const*>(mBuffer.data()) + mOffset);

		}
		else {
			std::strlen(mBuffer.data() + mOffset);
		}


		if (mReadMode) { value.resize(size); }
		rw_bytes<char>(value);
	}
};


struct Test {
	std::uint32_t value;

	void serialize(ByteStreami& stream) {
		stream.rw_u32(value);
	}
};
#endif