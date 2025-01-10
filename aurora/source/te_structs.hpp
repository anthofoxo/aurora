#pragma once

#include "te_stream.hpp"

#include "te_hash.hpp"

#include <any>
#include <array>
#include <string>

namespace aurora {
	bool is_known_step_type(std::string_view aStepType);
	bool is_known_tutorial_type(std::string_view aTutorial);

	enum struct DeclarationType : uint32_t {
		kLeaf = hash("SequinLeaf"),
		kSamp = hash("Sample"),
		kSpn = hash("EntitySpawner"),
		kMaster = hash("SequinMaster"),
		kDrawer = hash("SequinDrawer"),
		kGate = hash("SequinGate"),
		kLvl = hash("SequinLevel"),
		kPath = hash("Path"),
	};

	struct Datapoint final {
		float time;
		std::any value;
		std::string interpolation;
		std::string easing;

		void deserialize(ByteStream& aStream, uint32_t aDatatype);
	};

	struct TraitSelector {
		uint32_t selector;
		int32_t shareIdx;
	};

	struct Trait final {
		std::string object;
		std::vector<TraitSelector> selectors;
		uint32_t datatype;
		std::vector<Datapoint> datapoints;
		std::vector<Datapoint> editorDatapoints;

		uint32_t unknown1;
		uint32_t unknown2;
		uint32_t unknown3;
		uint32_t unknown4;
		uint32_t unknown5;

		std::string intensity0;
		std::string intensity1;

		uint8_t unknown6;
		uint8_t unknown7;
		uint32_t unknown8;

		float unknown9;
		float unknown10;
		float unknown11;
		float unknown12;
		float unknown13;

		uint8_t unknown14;
		uint8_t unknown15;
		uint8_t unknown16;

		void deserialize(ByteStream& aStream);
	};

	struct SampleEntry final {
		std::string sampleName;
		uint32_t loopBeats;
		uint32_t unknown2;

		void deserialize(ByteStream& aStream);
	};

	struct Subpath final {
		std::string string;
		uint32_t unknown2;
	};

	struct SequinLeaf final {
		static constexpr std::array<uint32_t, 4> kHeader = { 34, 33, 4, 2 };

		std::string _declaredName;
		size_t _beginOffset = 0;
		size_t _endOffset = 0;

		uint32_t header[4];
		uint32_t hash0;
		uint32_t unknown0;
		uint32_t hash1;
		std::string timeUnit;
		uint32_t hash2;
		std::vector<Trait> traits;
		uint32_t unknown1;
		std::vector<u32vec3> unknown2;
		uint32_t unknown3;
		uint32_t unknown4;
		uint32_t unknown5;

		void deserialize(ByteStream& aStream);
	};

	struct Sample final {
		static constexpr std::array<uint32_t, 2> kHeader = { 12, 4 };

		std::string _declaredName;
		size_t _beginOffset = 0;
		size_t _endOffset = 0;
		
		uint32_t header[2];
		std::optional<uint32_t> hash0; //EditStateComp
		std::string samplePlayMode;
		uint32_t unknown0;
		std::string filePath;

		//In the files, it's either a boolean then an int, or an int then a boolean. The order is unknown. I changed these on the debug version and they didn't do anything.
		uint8_t unknown1[5];

		float volume;
		float pitch;
		float pan;
		float offset;
		std::string channelGroup;

		void deserialize(ByteStream& aStream);
	};

	struct EntitySpawner final {
		static constexpr std::array<uint32_t, 3> kHeader = { 1, 4, 2 };

		std::string _declaredName;
		size_t _beginOffset = 0;
		size_t _endOffset = 0;

		uint32_t header[3];
		uint32_t hash0; //EditStateComp
		
		//WriteXfmComp
		uint32_t hash1; //XfmComp
		uint32_t unknown0;
		std::string xfmName;
		std::string constraint;
		
		Transform transform;
		
		uint32_t unknown2;
		std::string objlibPath;
		std::string bucketType;

		void deserialize(ByteStream& aStream);
	};

	struct SequinDrawer final {
		static constexpr std::array<uint32_t, 3> kHeader = { 7, 4, 1 };

		std::string _declaredName;
		size_t _beginOffset = 0;
		size_t _endOffset = 0;

		uint8_t header[3];
		uint32_t hash0;
		uint32_t unknown0;
		uint8_t unknownBool0;
		std::string drawLayers;
		std::string bucketType;
		uint32_t unknown1;

		void deserialize(ByteStream& aStream);
	};

	struct SequinMasterLvl final {
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

		void deserialize(ByteStream& aStream);
	};

	struct SequinGate final {
		struct GateEntry {
			uint32_t bucketHash;
			std::string lvlName;
			bool unknown1;
			std::string sentryType;
			uint32_t hash;
			uint32_t unknowncounter;

			void deserialize(ByteStream& aStream);
		};

		static constexpr std::array<uint32_t, 3> kHeader = { 26, 4, 1 };

		std::string _declaredName;
		size_t _beginOffset = 0;
		size_t _endOffset = 0;

		uint32_t header[3];
		uint32_t editStateComp;
		std::string spn;
		uint32_t unknown0;
		uint32_t spnParameter;
		int32_t unknown1; // -1

		std::vector<GateEntry> enteries;

		std::string preintro;
		std::string postintro;
		std::string restart;
		std::string unknownLvlParam;
		std::string sectionBossType;
		float unknown2;
		std::string randomFunction;

		void deserialize(ByteStream& aStream);
	};

	struct SequinMaster final {
		static constexpr std::array<uint32_t, 4> kHeader = { 33, 33, 4, 2 };

		std::string _declaredName;
		size_t _beginOffset = 0;
		size_t _endOffset = 0;

		std::array<uint32_t, 4> header = kHeader;
		uint32_t hash0;
		uint32_t unknown0 = 1;
		uint32_t hash1;
		std::string timeUnit = "kTimeBeats";
		uint32_t hash2;		//editstatecomp
		uint32_t unknown1 = 0;
		float unknown2;
		std::string skybox;
		std::string introLvl;

		std::vector<SequinMasterLvl> sublevels;

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
		std::string pathGameplay = "path.gameplay";

		void deserialize(ByteStream& aStream);
	};

	struct LvlLeafSequin final {
		uint32_t unknown0;
		uint32_t beatCount;
		uint8_t unknownBool1;
		std::string leafName;
		std::string defaultPath;
		std::vector<Subpath> subpaths;
		std::string stepType; // see: is_known_step_type

		uint32_t unknown1;

		Transform transform;

		uint8_t unknownBool2;
		uint8_t unknownBool3;
		uint8_t continuation;

		void deserialize(ByteStream& aStream);
	};

	struct SequinLevel final {
		static constexpr std::array<uint32_t, 4> kHeader = { 51, 33, 4, 2 };

		std::string _declaredName;
		size_t _beginOffset = 0;
		size_t _endOffset = 0;

		uint32_t header[4];
		uint32_t hash0;
		uint32_t unknown0;
		uint32_t hash1;
		std::string timeUnit;
		uint32_t unknown1;
		uint32_t unknownInt0;
		uint32_t editStateCompHash;
		std::vector<Trait> traits;
		uint32_t unknown2;
		std::string phaseMoveType;
		uint32_t unknown3;
		uint8_t unknown5;

		// One of the rare cases where the size of the array size isnt first,
		// only way we currently know about this is via a continuation byte
		std::vector<LvlLeafSequin> leafSequin;

		std::vector<SampleEntry> sampNames;

		uint8_t unknownBool5;

		float unknownFloat12;
		std::string flowref;
		std::vector<TraitSelector> traitSelectors;
		std::string kNumTraitType;	//available options are		kNumTraitInterps	kNumTraitTypes
		uint8_t unknownBool6;
		std::string tutorialType; // See: is_known_tutorial_type

		float footer1;
		float footer2;
		float footer3;

		void deserialize(ByteStream& aStream);
	};

	struct LibraryImport final {
		uint32_t unknown0;
		std::string library;
	};

	struct ObjectImport final {
		uint32_t type;
		std::string name;
		uint32_t unknown0;
		std::string library;
	};

	struct ObjectDeclaration final {
		size_t _definitionOffset = 0;

		DeclarationType type;
		std::string name;
	};

	struct Path final {
		static constexpr std::array<uint32_t, 3> kHeader = { 41, 4, 1 };

		std::string _declaredName;
		size_t _beginOffset = 0;
		size_t _endOffset = 0;

		uint32_t header[3];
		uint32_t hash0; //editstatecomp

		f32vec3 scale0;

		f32vec3 scale1;

		uint32_t unknown6;

		std::string meshName; //lattice_5.mesh
		bool unknownBool0;
		std::string pathInterpType; //kPathScaleInterpLinear
		uint32_t unknown7;
		uint8_t unknown8;
		uint8_t unknown9;
		std::vector<std::string> decorators;

		bool unknownBool1;

		void deserialize(ByteStream& aStream);
	};

	struct ObjlibLevel final {
		std::vector<std::byte> _bytes;
		std::vector<SequinLeaf> _leafs;
		std::vector<Sample> _samps;
		std::vector<EntitySpawner> _spns;
		std::vector<SequinMaster> _masters;
		std::vector<SequinDrawer> _drawers;
		std::vector<SequinLevel> _lvls;
		std::vector<SequinGate> _gates;
		std::vector<Path> _paths;

		uint32_t filetype; // 0x8
		uint32_t objlibType; // 0x19621c9d
		uint32_t unknown0;
		uint32_t unknown1;
		uint32_t unknown2;
		uint32_t unknown3;
		std::vector<LibraryImport> libraryImports;
		std::string origin;
		std::vector<ObjectImport> objectImports;
		std::vector<ObjectDeclaration> objectDeclarations;

		// Object definitions are dumped here

		// Footer

		// THESE REMAIN UNSET ATM
		std::string sceneName;
		std::string lowSpecScene;
		std::string vrSettings;
		std::string environment;
		std::string playerCamera;
		std::string unknownfooteritem; //nx.cam
		std::string playerCam2; // duplicate of camera
		float bpm;
		std::string avatar;
		std::string master;
		std::string drawer;
		std::string masterChannel;
		std::string baseChannel;
		std::string realtimeChannel;
		bool unknownFooterval;
		// !--

		void deserialize(ByteStream& aStream);
	};
}