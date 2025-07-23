#pragma once

#include <any>
#include <cstdint>
#include <string>
#include <vector>

#include "au_hash.hpp"
#include "au_serial.hpp"

#include <glm/glm.hpp>

namespace thumper {
enum struct DeclarationType : std::uint32_t {
	kLeaf = aurora::fnv1a("SequinLeaf"),
	kSamp = aurora::fnv1a("Sample"),
	kSpn = aurora::fnv1a("EntitySpawner"),
	kMaster = aurora::fnv1a("SequinMaster"),
	kDrawer = aurora::fnv1a("SequinDrawer"),
	kGate = aurora::fnv1a("SequinGate"),
	kLvl = aurora::fnv1a("SequinLevel"),
	kPath = aurora::fnv1a("Path"),
};

struct Transform final : public aurora::Serializable {
	glm::f32vec3 position;
	glm::f32vec3 rotx;
	glm::f32vec3 roty;
	glm::f32vec3 rotz;
	glm::f32vec3 scale;

	void serialize(aurora::Serializer& a) {
		AU_FIELD(a, position);
		AU_FIELD(a, rotx);
		AU_FIELD(a, roty);
		AU_FIELD(a, rotz);
		AU_FIELD(a, scale);
	}
};

struct LevelListing final : public aurora::Serializable {
	struct Entry final : public aurora::Serializable {
		Entry() = default;
		Entry(std::string key, std::uint32_t unknown0, std::string path, std::string unlocks, bool defaultLocked, bool unknown1, bool credits, std::uint32_t colorIdx0,
		      std::uint32_t colorIdx1)
		    : key(std::move(key)),
		      unknown0(unknown0),
		      path(std::move(path)),
		      unlocks(std::move(unlocks)),
		      defaultLocked(defaultLocked),
		      unknown1(unknown1),
		      credits(credits),
		      colorIdx0(colorIdx0),
		      colorIdx1(colorIdx1) {}

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
		a.serialize(nullptr, entries);  // special
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

		void serialize(aurora::Serializer& a) override { AU_FIELD(a, elements); }
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

struct LevelObjLib : public aurora::Serializable {
	static constexpr std::array<std::uint32_t, 4> kHeader = { 33, 19, 15, 4 };

	std::uint32_t fileType;
	std::uint32_t objlibType;

	std::array<std::uint32_t, 4> header;

	struct GlobalLibImport : public aurora::Serializable {
		std::uint32_t unknown0;
		std::string library;

		void serialize(aurora::Serializer& a) override {
			AU_FIELD(a, unknown0);
			AU_FIELD(a, library);
		}
	};

	struct ObjectImport : public aurora::Serializable {
		std::uint32_t type;
		std::string name;
		std::uint32_t unknown0;
		std::string library;

		void serialize(aurora::Serializer& a) override {
			AU_FIELD(a, type);
			AU_FIELD(a, name);
			AU_FIELD(a, unknown0);
			AU_FIELD(a, library);
		}
	};

	struct ObjectListEntry : public aurora::Serializable {
		std::uint32_t objectType;
		std::string objectName;

		void serialize(aurora::Serializer& a) override {
			AU_FIELD(a, objectType);
			AU_FIELD(a, objectName);
		}
	};

	struct ObjectDeclaration : public aurora::Serializable {
		std::uint32_t declarationType;  // in old aurora this was a custom vartype using hashes to decide the type.
		std::string name;

		void serialize(aurora::Serializer& a) override {
			AU_FIELD(a, declarationType);
			AU_FIELD(a, name);
		}
	};

	struct Path : public aurora::Serializable {
		static constexpr std::array<std::uint32_t, 2> kHeader = { 41, 4 };

		std::array<std::uint32_t, 2> header;

		std::uint32_t unknown0;
		std::uint32_t hash0;  // editstatecomp

		glm::f32vec3 scale0;
		glm::f32vec3 scale1;

		std::optional<std::uint32_t> unknown6;  // Exists if unknown0 is ==1. originally type std optional

		std::string meshName;  // lattice_5.mesh
		bool unknownBool0;
		std::string pathInterpType;  // kPathScaleInterpLinear
		std::uint32_t unknown7;
		std::uint8_t unknown8;
		std::uint8_t unknown9;
		std::vector<std::string> decorators;

		bool unknownBool1;

		void serialize(aurora::Serializer& a) {
			AU_FIELD(a, header);
			for (std::size_t i = 0; i < header.size(); i++) {
				assert(header[i] == kHeader[i]);
			}

			AU_FIELD(a, unknown0);
			AU_FIELD(a, hash0);

			AU_FIELD(a, scale0);
			AU_FIELD(a, scale1);

			AU_FIELD(a, unknown6, unknown0 == 1);

			AU_FIELD(a, meshName);
			AU_FIELD(a, unknownBool0);
			AU_FIELD(a, pathInterpType);
			AU_FIELD(a, unknown7);
			AU_FIELD(a, unknown8);
			AU_FIELD(a, unknown9);
			AU_FIELD(a, decorators);

			AU_FIELD(a, unknownBool1);
		}
	};

	struct TraitSelector : public aurora::Serializable {
		std::uint32_t selector;
		std::int32_t shareIdx;

		void serialize(aurora::Serializer& a) override {
			AU_FIELD(a, selector);
			AU_FIELD(a, shareIdx);
		}
	};

	// any cast dont work. DO NOT USE DATAPOINT IN ITS CURRENT STATE
	struct Datapoint : public aurora::Serializable {
		float time;
		std::any value;
		std::string interpolationType;
		std::string easingType;

		void serialize(aurora::Serializer& a) override {
			AU_FIELD(a, time);
			// AU_FIELD(a, value);
			AU_FIELD(a, interpolationType);
			AU_FIELD(a, easingType);
		}
	};

	struct SequinLeafTrait : public aurora::Serializable {
		std::string objectName;
		std::vector<TraitSelector> selectors;
		std::uint32_t dataType;
		std::vector<Datapoint> datapoints;
		std::vector<Datapoint> editorDatapoints;

		std::uint32_t unknown1;
		std::uint32_t unknown2;
		std::uint32_t unknown3;
		std::uint32_t unknown4;
		std::uint32_t unknown5;

		std::string intensity0;
		std::string intensity1;

		std::uint8_t unknown6;
		std::uint8_t unknown7;
		std::uint32_t unknown8;

		float unknown9;
		float unknown10;
		float unknown11;
		float unknown12;
		float unknown13;

		std::uint8_t unknown14;
		std::uint8_t unknown15;
		std::uint8_t unknown16;

		void serialize(aurora::Serializer& a) override {
			AU_FIELD(a, objectName);
			AU_FIELD(a, selectors);
			AU_FIELD(a, dataType);
			AU_FIELD(a, datapoints);
			AU_FIELD(a, editorDatapoints);

			AU_FIELD(a, unknown1);
			AU_FIELD(a, unknown2);
			AU_FIELD(a, unknown3);
			AU_FIELD(a, unknown4);
			AU_FIELD(a, unknown5);

			AU_FIELD(a, intensity0);
			AU_FIELD(a, intensity1);

			AU_FIELD(a, unknown6);
			AU_FIELD(a, unknown7);
			AU_FIELD(a, unknown8);

			AU_FIELD(a, unknown9);
			AU_FIELD(a, unknown10);
			AU_FIELD(a, unknown11);
			AU_FIELD(a, unknown12);
			AU_FIELD(a, unknown13);

			AU_FIELD(a, unknown14);
			AU_FIELD(a, unknown15);
			AU_FIELD(a, unknown16);
		}
	};

	struct SequinLeaf : public aurora::Serializable {
		static constexpr std::array<std::uint32_t, 4> kHeader = { 34, 33, 4, 2 };

		std::array<std::uint32_t, 4> header;
		float unknownFloat0;  // 23.88671875 ? from a random leaf in level 6
		// uint32_t unknown0;
		// uint32_t hash1;		//not sure if these two are used but easy to re-enable
		std::string timeUnit;
		std::uint32_t EditStateCompHash;

		std::uint32_t sequinLeafTraitCount;
		std::vector<SequinLeafTrait> SequinLeafTraits;

		std::uint32_t unknown1;
		std::vector<glm::u32vec3> unknown2;
		std::uint32_t unknown3;
		std::uint32_t unknown4;
		std::uint32_t unknown5;

		void serialize(aurora::Serializer& a) override {
			AU_FIELD(a, header);
			for (std::size_t i = 0; i < header.size(); i++) {
				assert(header[i] == kHeader[i]);
			}

			AU_FIELD(a, unknownFloat0);
			// AU_FIELD(a, unknown0);
			// AU_FIELD(a, hash1);
			AU_FIELD(a, timeUnit);
			AU_FIELD(a, EditStateCompHash);

			AU_FIELD(a, sequinLeafTraitCount);
			AU_FIELD(a, SequinLeafTraits);

			AU_FIELD(a, unknown1);
			AU_FIELD(a, unknown2);
			AU_FIELD(a, unknown3);
			AU_FIELD(a, unknown4);
			AU_FIELD(a, unknown5);
		}
	};

	struct Sample : public aurora::Serializable {
		static constexpr std::array<std::uint32_t, 2> kHeader = { 12, 4 };

		std::array<std::uint32_t, 2> header;

		std::uint32_t hash0;  // EditStateComp, this was original standard optional but I changed it so the serializer likes it
		std::string samplePlayMode;
		std::uint32_t unknown0;
		std::string filepath;

		// In the files, it's either a boolean then an int, or an int then a boolean. The order is unknown.
		std::uint8_t unknown1[5];

		float volume;
		float pitch;
		float pan;
		float offset;
		std::string channelGroup;

		void serialize(aurora::Serializer& a) override {
			AU_FIELD(a, header);

			for (std::size_t i = 0; i < kHeader.size(); i++) {
				assert(kHeader[i] == header[i]);
			}

			AU_FIELD(a, hash0);
			AU_FIELD(a, samplePlayMode);
			AU_FIELD(a, unknown0);
			AU_FIELD(a, filepath);

			AU_FIELD(a, unknown1[0]);
			AU_FIELD(a, unknown1[1]);
			AU_FIELD(a, unknown1[2]);
			AU_FIELD(a, unknown1[3]);
			AU_FIELD(a, unknown1[4]);

			AU_FIELD(a, volume);
			AU_FIELD(a, pitch);
			AU_FIELD(a, pan);
			AU_FIELD(a, offset);
			AU_FIELD(a, channelGroup);
		}
	};

	struct Trait : public aurora::Serializable {  // for any trait system. Mainly seen in SequinLeaf and SequinLevel but MIGHT be in TraitAnim too.
		std::string objectName;
		std::vector<TraitSelector> selectors;
		std::uint32_t datatype;
		std::vector<Datapoint> datapoints;
		std::vector<Datapoint> editorDatapoints;

		std::uint32_t unknown1;
		std::uint32_t unknown2;
		std::uint32_t unknown3;
		std::uint32_t unknown4;
		std::uint32_t unknown5;

		std::string intensity0;
		std::string intensity1;

		std::uint8_t unknown6;
		std::uint8_t unknown7;
		std::uint32_t unknown8;

		float unknown9;
		float unknown10;
		float unknown11;
		float unknown12;
		float unknown13;

		std::uint8_t unknown14;
		std::uint8_t unknown15;
		std::uint8_t unknown16;

		void serialize(aurora::Serializer& a) override {
			AU_FIELD(a, objectName);
			AU_FIELD(a, selectors);
			AU_FIELD(a, datatype);
			AU_FIELD(a, datapoints);
			AU_FIELD(a, editorDatapoints);

			AU_FIELD(a, unknown1);
			AU_FIELD(a, unknown2);
			AU_FIELD(a, unknown3);
			AU_FIELD(a, unknown4);
			AU_FIELD(a, unknown5);

			AU_FIELD(a, intensity0);
			AU_FIELD(a, intensity1);

			AU_FIELD(a, unknown6);
			AU_FIELD(a, unknown7);
			AU_FIELD(a, unknown8);

			AU_FIELD(a, unknown9);
			AU_FIELD(a, unknown10);
			AU_FIELD(a, unknown11);
			AU_FIELD(a, unknown12);
			AU_FIELD(a, unknown13);

			AU_FIELD(a, unknown14);
			AU_FIELD(a, unknown15);
			AU_FIELD(a, unknown16);
		}
	};

	struct LvlLeafSequin : public aurora::Serializable {  // for entry of Leafs inside a Level file

		// fill in later cuz im retard ed

		void serialize(aurora::Serializer& a) override {}
	};

	struct SampleEntry : public aurora::Serializable {  // for entry for samples inside a Level file
		std::string sampleName;
		std::uint32_t loopBeats;
		std::uint32_t unknown0;

		void serialize(aurora::Serializer& a) override {
			AU_FIELD(a, sampleName);
			AU_FIELD(a, loopBeats);
			AU_FIELD(a, unknown0);
		}
	};

	struct EntitySpawner : public aurora::Serializable {
		static constexpr std::array<std::uint32_t, 3> kHeader = { 1, 4, 2 };

		std::array<std::uint32_t, 3> header;

		std::uint32_t unknown0;  // what is this?
		std::uint32_t hash0;     // EditStateComp

		// WriteXfmComp
		std::uint32_t hash1;  // XfmComp
		std::uint32_t unknown1;
		std::string xfmName;
		std::string constraint;

		Transform transform;

		std::uint32_t unknown2;
		std::string objlibPath;
		std::string bucketType;

		void serialize(aurora::Serializer& a) override {
			AU_FIELD(a, header);
			for (std::size_t i = 0; i < kHeader.size(); ++i) {
				assert(header[i] == kHeader[i]);
			}

			AU_FIELD(a, unknown0);
			AU_FIELD(a, hash0);

			AU_FIELD(a, hash1);
			AU_FIELD(a, unknown1);
			AU_FIELD(a, xfmName);
			AU_FIELD(a, constraint);

			AU_FIELD(a, transform);

			AU_FIELD(a, unknown2);
			AU_FIELD(a, objlibPath);
			AU_FIELD(a, bucketType);
		}
	};

	struct SequinDrawer : public aurora::Serializable {
		static constexpr std::array<std::uint32_t, 3> kHeader = { 7, 4, 1 };

		std::array<std::uint32_t, 3> header;

		std::uint32_t hash0;
		std::uint32_t unknown0;
		std::uint8_t unknownBool0;
		std::string drawLayers;
		std::string bucketType;
		std::uint32_t unknown1;

		void serialize(aurora::Serializer& a) override {
			AU_FIELD(a, header);

			for (std::size_t i = 0; i < kHeader.size(); i++) {
				assert(header[i] == kHeader[i]);
			}

			AU_FIELD(a, hash0);
			AU_FIELD(a, unknown0);
			AU_FIELD(a, unknownBool0);
			AU_FIELD(a, drawLayers);
			AU_FIELD(a, bucketType);
			AU_FIELD(a, unknown1);
		}
	};

	struct SequinMasterLevel : public aurora::Serializable {
		std::string lvlName;
		std::string gateName;
		bool isCheckpoint;
		std::string checkpointLeaderLvlName;
		std::string restLvlName;

		uint8_t unknownBool0;
		uint8_t unknownBool1;
		uint32_t unknown0;
		uint8_t unknownBool2;

		bool playPlus;

		void serialize(aurora::Serializer& a) override {
			AU_FIELD(a, lvlName);
			AU_FIELD(a, gateName);
			AU_FIELD(a, isCheckpoint);
			AU_FIELD(a, checkpointLeaderLvlName);
			AU_FIELD(a, restLvlName);

			AU_FIELD(a, unknownBool0);
			AU_FIELD(a, unknownBool1);
			AU_FIELD(a, unknown0);
			AU_FIELD(a, unknownBool2);
		}
	};

	struct SequinMaster : public aurora::Serializable {
		static constexpr std::array<std::uint32_t, 4> kHeader = { 33, 33, 4, 2 };

		std::array<std::uint32_t, 4> header;
		std::uint32_t hash0;
		std::uint32_t unknown0;
		std::uint32_t hash1;
		std::string timeUnit;
		std::uint32_t hash2;  // editstatecomp
		std::uint32_t unknown1;
		float unknown2;
		std::string skybox;
		std::string introLvl;

		std::vector<SequinMasterLevel> sublevels;

		uint8_t footer1;
		uint8_t footer2;
		uint32_t footer3;
		uint32_t footer4;
		uint32_t footer5;
		uint32_t footer6;
		float footer7;
		float footer8;
		float footer9;
		std::string checkpointLvl;
		std::string pathGameplay;

		void serialize(aurora::Serializer& a) override {
			AU_FIELD(a, header);
			for (std::size_t i = 0; i < header.size(); i++) {
				assert(header[i] == kHeader[i]);
			}

			AU_FIELD(a, hash0);
			AU_FIELD(a, unknown0);
			AU_FIELD(a, hash1);
			AU_FIELD(a, timeUnit);
			AU_FIELD(a, hash2);
			AU_FIELD(a, unknown1);
			AU_FIELD(a, unknown2);
			AU_FIELD(a, skybox);
			AU_FIELD(a, introLvl);

			AU_FIELD(a, sublevels);

			AU_FIELD(a, footer1);
			AU_FIELD(a, footer2);
			AU_FIELD(a, footer3);
			AU_FIELD(a, footer4);
			AU_FIELD(a, footer5);
			AU_FIELD(a, footer6);
			AU_FIELD(a, footer7);
			AU_FIELD(a, footer8);
			AU_FIELD(a, footer9);

			AU_FIELD(a, checkpointLvl);
			AU_FIELD(a, pathGameplay);
		}
	};

	struct SequinGate : public aurora::Serializable {
		struct GateEntry : public aurora::Serializable {
			uint32_t bucketHash;
			std::string lvlName;
			bool unknown1;
			std::string sentryType;
			uint32_t hash;
			uint32_t unknownCounter;

			void serialize(aurora::Serializer& a) override {
				AU_FIELD(a, bucketHash);
				AU_FIELD(a, lvlName);
				AU_FIELD(a, unknown1);
				AU_FIELD(a, sentryType);
				AU_FIELD(a, hash);
				AU_FIELD(a, unknownCounter);
			}
		};

		static constexpr std::array<std::uint32_t, 3> kHeader = { 26, 4, 1 };

		std::array<std::uint32_t, 3> header;
		std::uint32_t editStateComp;
		std::string spn;
		std::uint32_t unknown0;
		std::uint32_t spnParameter;
		std::int32_t unknown1;  // -1

		std::vector<GateEntry> enteries;

		std::string preintro;
		std::string postintro;
		std::string restart;
		std::string unknownLvlParam;
		std::string sectionBossType;
		float unknown2;
		std::string randomFunction;

		void deserialize(aurora::Serializer& a) {
			AU_FIELD(a, header);
			for (std::size_t i = 0; i < kHeader.size(); i++) {
				assert(header[i] == kHeader[i]);
			}

			AU_FIELD(a, editStateComp);
			AU_FIELD(a, spn);
			AU_FIELD(a, unknown0);
			AU_FIELD(a, spnParameter);
			AU_FIELD(a, unknown1);

			AU_FIELD(a, enteries);

			AU_FIELD(a, preintro);
			AU_FIELD(a, postintro);
			AU_FIELD(a, restart);
			AU_FIELD(a, unknownLvlParam);
			AU_FIELD(a, sectionBossType);
			AU_FIELD(a, unknown2);
			AU_FIELD(a, randomFunction);
		}
	};

	struct SequinLevel : public aurora::Serializable {
		static constexpr std::array<uint32_t, 4> kHeader = { 0, 0, 0, 0 };  // TODO: Fill this in

		std::array<uint32_t, 4> header;
		std::uint32_t hash0;
		std::uint32_t unknown0;
		std::uint32_t hash1;
		std::string timeUnit;
		std::uint32_t unknown1;
		std::uint32_t unknownInt0;
		std::uint32_t editStateCompHash;
		std::vector<Trait> traits;
		std::uint32_t unknown2;
		std::string phaseMoveType;
		std::uint32_t unknown3;

		// there is a rare case where the size of the array ISN'T first.
		// only way we know about this is via this continuation byte.
		std::uint8_t unknown5;

		std::vector<LvlLeafSequin> leafSequin;

		std::vector<SampleEntry> sampNames;

		std::uint8_t unknownBool0;

		std::string flowRef;
		std::vector<TraitSelector> traitSelectors;
		std::string kNumTraitType;  // available options are		kNumTraitInterps	kNumTraitTypes
		std::uint8_t unknownBool6;
		std::string tutorialType;  // See: is_known_tutorial_type

		float footer1;
		float footer2;
		float footer3;

		void serialize(aurora::Serializer& a) override {
			AU_FIELD(a, header);

			for (int i = 0; i < kHeader.size(); i++) {
				assert(header[i] == kHeader[i]);
			}

			AU_FIELD(a, hash0);
			AU_FIELD(a, unknown0);
			AU_FIELD(a, hash1);
			AU_FIELD(a, timeUnit);
			AU_FIELD(a, unknown1);
			AU_FIELD(a, unknownInt0);
			AU_FIELD(a, editStateCompHash);
			AU_FIELD(a, traits);
			AU_FIELD(a, unknown2);
			AU_FIELD(a, phaseMoveType);
			AU_FIELD(a, unknown3);

			AU_FIELD(a, unknown5);  // not sure if this is present.

			AU_FIELD(a, leafSequin);

			AU_FIELD(a, sampNames);

			AU_FIELD(a, unknownBool0);

			AU_FIELD(a, flowRef);
			AU_FIELD(a, traitSelectors);
			AU_FIELD(a, kNumTraitType);
			AU_FIELD(a, unknownBool6);
			AU_FIELD(a, tutorialType);

			AU_FIELD(a, footer1);
			AU_FIELD(a, footer2);
			AU_FIELD(a, footer3);
		}
	};

	uint32_t globalImportCount;
	std::vector<GlobalLibImport> globalImports;  // size = globalImportCount

	std::string objlibName;  //	--	NOTE: Reading up to here is a good start on reading objlibs. Test by checking multiple types of objlib - not just LevelLib. check GfxLib as well

	uint32_t objectImportCount;  // for object specific import? unknown. probably.
	std::vector<ObjectImport> objectImports;

	uint32_t objectCount;
	std::vector<ObjectListEntry> objectList;

	// after this is declared objects
	// x

	// footer now
};

}  // namespace thumper