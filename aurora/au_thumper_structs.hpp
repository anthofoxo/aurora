#pragma once

#include <string>
#include <vector>

#include "au_serial.hpp"

namespace thumper {
	struct LevelListing final : public aurora::Serializable {
		struct Entry final : public aurora::Serializable {
			Entry() = default;
			Entry(
				std::string key,
				std::uint32_t unknown0,
				std::string path,
				std::string unlocks,
				bool defaultLocked,
				bool unknown1,
				bool credits,
				std::uint32_t colorIdx0,
				std::uint32_t colorIdx1
			)
				: key(std::move(key))
				, unknown0(unknown0)
				, path(std::move(path))
				, unlocks(std::move(unlocks))
				, defaultLocked(defaultLocked)
				, unknown1(unknown1)
				, credits(credits)
				, colorIdx0(colorIdx0)
				, colorIdx1(colorIdx1)
			{
			}

			std::string key;
			std::uint32_t unknown0;
			std::string path;
			std::string unlocks;
			bool defaultLocked;
			bool unknown1;
			bool credits;
			std::uint32_t colorIdx0;
			std::uint32_t colorIdx1;

			void serialize(aurora::Serializer& a) override {
				AU_FIELD(a, key);
				AU_FIELD(a, unknown0);
				AU_FIELD(a, path);
				AU_FIELD(a, unlocks);
				AU_FIELD(a, defaultLocked);
				AU_FIELD(a, unknown1);
				AU_FIELD(a, credits);
				AU_FIELD(a, colorIdx0);
				AU_FIELD(a, colorIdx1);
			};
		};

		std::vector<Entry> entries;

		void serialize(aurora::Serializer& a) override {
			a.serialize(nullptr, entries); // special
		}
	};

	struct Credits : public aurora::Serializable {
		struct MajorGroupElement : public aurora::Serializable {
			std::string decoration;
			std::string text;

			void serialize(aurora::Serializer& a) override {
				AU_FIELD(a, decoration);
				AU_FIELD(a, text);
			}
		};

		struct MajorGroup : public aurora::Serializable {
			std::vector<MajorGroupElement> elements;

			void serialize(aurora::Serializer& a) override {
				AU_FIELD(a, elements);
			}
		};

		struct MinorGroup : public aurora::Serializable {
			std::string banner;
			std::vector<std::string> thanks;

			void serialize(aurora::Serializer& a) override {
				AU_FIELD(a, banner);
				AU_FIELD(a, thanks);
			}
		};

		std::vector<MajorGroup> main;
		std::vector<MinorGroup> tail;

		void serialize(aurora::Serializer& a) override {
			AU_FIELD(a, main);
			AU_FIELD(a, tail);
		}
	};
}