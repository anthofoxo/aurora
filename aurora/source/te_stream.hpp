#pragma once

#include "te_types.hpp"

#include <vector>
#include <filesystem>
#include <cstdint>
#include <cstddef>
#include <cassert>
#include <string>
#include <fstream>
#include <cstddef>

namespace aurora {
	struct ByteStream final {
		ByteStream() = default;
		ByteStream(std::filesystem::path const& aPath) {
			std::ifstream in(aPath, std::ios::in | std::ios::binary);
			in.seekg(0, std::ios::end);
			mData.resize(in.tellg());
			in.seekg(0, std::ios::beg);
			in.read(reinterpret_cast<char*>(mData.data()), mData.size());
		}
	
		inline int8_t poke_s8() const { return *reinterpret_cast<int8_t const*>(head()); }
		inline uint8_t poke_u8() const { return *reinterpret_cast<uint8_t const*>(head()); }
		inline int16_t poke_s16() const { return *reinterpret_cast<int16_t const*>(head()); }
		inline uint16_t poke_u16() const { return *reinterpret_cast<uint16_t const*>(head()); }
		inline int32_t poke_s32() const { return *reinterpret_cast<int32_t const*>(head()); }
		inline uint32_t poke_u32() const { return *reinterpret_cast<uint32_t const*>(head()); }
		inline float poke_f32() const { return *reinterpret_cast<float const*>(head()); }
		inline u32vec3 poke_u32vec3() const { return *reinterpret_cast<u32vec3 const*>(head()); }
		inline f32vec3 poke_f32vec3() const { return *reinterpret_cast<f32vec3 const*>(head()); }
		inline f32vec4 poke_f32vec4() const { return *reinterpret_cast<f32vec4 const*>(head()); }
		inline Transform poke_transform() const { return *reinterpret_cast<Transform const*>(head()); }

		inline int8_t read_s8() { auto val = poke_s8(); advance(sizeof(int8_t)); return val; }
		inline uint8_t read_u8() { auto val = poke_u8(); advance(sizeof(uint8_t)); return val; }
		inline int16_t read_s16() { auto val = poke_s16(); advance(sizeof(int16_t)); return val; }
		inline uint16_t read_u16() { auto val = poke_u16(); advance(sizeof(uint16_t)); return val; }
		inline int32_t read_s32() { auto val = poke_s32(); advance(sizeof(int32_t)); return val; }
		inline uint32_t read_u32() { auto val = poke_u32(); advance(sizeof(uint32_t)); return val; }
		inline float read_f32() { auto val = poke_f32(); advance(sizeof(float)); return val; }
		inline u32vec3 read_u32vec3() { auto val = poke_u32vec3(); advance(sizeof(u32vec3)); return val; }
		inline f32vec3 read_f32vec3() { auto val = poke_f32vec3(); advance(sizeof(f32vec3)); return val; }
		inline f32vec4 read_f32vec4() { auto val = poke_f32vec4(); advance(sizeof(f32vec4)); return val; }
		inline Transform read_transform() { auto val = poke_transform(); advance(sizeof(Transform)); return val; }

		// Write apis ignore stream offset/mark
		inline void write_bool(bool v) { size_t current = mData.size(); for (auto i = 0; i < sizeof(v); ++i) mData.emplace_back(0); *reinterpret_cast<uint8_t*>(mData.data() + current) = v; }
		inline void write_s8(int8_t v) { size_t current = mData.size(); for (auto i = 0; i < sizeof(v); ++i) mData.emplace_back(0); *reinterpret_cast<int8_t*>(mData.data() + current) = v; }
		inline void write_u8(uint8_t v) { size_t current = mData.size(); for (auto i = 0; i < sizeof(v); ++i) mData.emplace_back(0); *reinterpret_cast<uint8_t*>(mData.data() + current) = v; }
		inline void write_s16(int16_t v) { size_t current = mData.size(); for (auto i = 0; i < sizeof(v); ++i) mData.emplace_back(0); *reinterpret_cast<int32_t*>(mData.data() + current) = v; }
		inline void write_u16(uint16_t v) { size_t current = mData.size(); for (auto i = 0; i < sizeof(v); ++i) mData.emplace_back(0); *reinterpret_cast<uint16_t*>(mData.data() + current) = v; }
		inline void write_s32(int32_t v) { size_t current = mData.size(); for (auto i = 0; i < sizeof(v); ++i) mData.emplace_back(0); *reinterpret_cast<int32_t*>(mData.data() + current) = v; }
		inline void write_u32(uint32_t v) { size_t current = mData.size(); for (auto i = 0; i < sizeof(v); ++i) mData.emplace_back(0); *reinterpret_cast<uint32_t*>(mData.data() + current) = v; }
		inline void write_f32(float v) { size_t current = mData.size(); for (auto i = 0; i < sizeof(v); ++i) mData.emplace_back(0); *reinterpret_cast<float*>(mData.data() + current) = v; }
		inline void write_u32vec3(u32vec3 const& v) { size_t current = mData.size(); for (auto i = 0; i < sizeof(v); ++i) mData.emplace_back(0); *reinterpret_cast<u32vec3*>(mData.data() + current) = v; }
		inline void write_f32vec3(f32vec3 const& v) { size_t current = mData.size(); for (auto i = 0; i < sizeof(v); ++i) mData.emplace_back(0); *reinterpret_cast<f32vec3*>(mData.data() + current) = v; }
		inline void write_f32vec4(f32vec4 const& v) { size_t current = mData.size(); for (auto i = 0; i < sizeof(v); ++i) mData.emplace_back(0); *reinterpret_cast<f32vec4*>(mData.data() + current) = v; }
		inline void write_transform(Transform const& v) { size_t current = mData.size(); for (auto i = 0; i < sizeof(v); ++i) mData.emplace_back(0); *reinterpret_cast<Transform*>(mData.data() + current) = v; }

		inline void write_str(std::string const& v) {
			write_u32(v.size());
			size_t current = mData.size();
			for (auto i = 0; i < v.size(); ++i) mData.emplace_back(0);
			memcpy(mData.data() + current, v.data(), v.size());
		}
		// ---

		inline std::string read_str() {
			uint32_t length = read_u32();
			std::string str;
			str.resize(length);
			memcpy(str.data(), head(), length);
			advance(length);
			return str;
		}


		inline std::byte const* head() const { return mData.data() + mOffset; }

		void advance(size_t aRelativeOffset) {
			assert(mOffset + aRelativeOffset <= mData.size());
			mOffset += aRelativeOffset;
		}

		inline char const* charhead() const { return reinterpret_cast<char const*>(mData.data()) + mOffset; }

		std::vector<std::byte> mData;
		size_t mOffset = 0;
	};
}