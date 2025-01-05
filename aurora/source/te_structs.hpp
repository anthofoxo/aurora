#pragma once

#include "te_stream.hpp"

#include "te_hash.hpp"

#include <any>
#include <array>
#include <string>

namespace aurora {
	enum struct DeclarationType : uint32_t {
		kLeaf = 0xce7e85f6, // SequinLeaf
		kSamp = 0x7aa8f390, // Sample
		kSpn = 0xd897d5db, // EntitySpawner
		kMaster = 0x490780b9, // SequinMaster
		kDrawer = 0xd3058b5d, // SequinDrawer
		kGate = 0xaa63a508, // SequinGate
		kLvl = 0xbcd17473, // SequinLevel
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

	struct SampleEntry final {	// correct
		std::string sampleName;
		uint32_t loopBeats;
		uint32_t unknown2;
	};

	struct Subpath final {
		std::string string;
		uint32_t unknown2;
	};

	struct LvlLeafSequin final {
		uint32_t beatCount;
		uint8_t unknownBool1;
		std::string leafName;
		std::string defaultPath;
		std::vector<Subpath> subpaths;
		std::string stepType;	//available options are		kStepAny	kStepFirst	kStepGameplay	kStepLast	kStepProp

		uint32_t unknown1;

		Transform transform;


		uint8_t unknownBool2;
		uint8_t unknownBool3;
		uint8_t continuation;
		
		std::vector<SampleEntry> sampNames;
		

		void deserialize(ByteStream& aStream);
	};


	struct Leaf final {
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

	struct Samp final {
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

	struct Spn final {
		std::string _declaredName;
		size_t _beginOffset = 0;
		size_t _endOffset = 0;

		uint32_t header[3]; // 1, 4, 2
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
		std::string _declaredName;
		size_t _beginOffset = 0;
		size_t _endOffset = 0;

		uint8_t header[3]; //7, 4, 1
		uint32_t hash0;
		uint32_t unknown0;
		uint8_t unknownBool0;
		std::string drawLayers;
		std::string bucketType;
		uint32_t unknown1;

		void deserialize(ByteStream& aStream);
	};

	struct SequinMasterLvl final {
		std::string lvlName;	//can be 00 00 00 00 if there IS a boss lvl here
		std::string gateName;	//can be 00 00 00 00 if there is no boss lvl here
		bool isCheckpoint;
		std::string checkpointLeaderLvlName;
		std::string restLvlName;

		//what the heck are these
		uint8_t unknownBool0;
		uint8_t unknownBool1;
		uint32_t unknown0;		//either an int or 4 bools.
		uint8_t unknownBool2;
		//uint8_t unknownBool3;
		

		bool playPlus;

		void deserialize(ByteStream& aStream);
	};

	struct Gate final {
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

	//using level 2's sequin master as the example, offset 0x346B
	struct SequinMaster final {
		static constexpr std::array<uint32_t, 4> kHeader = { 33, 33, 4, 2 };

		std::string _declaredName;
		size_t _beginOffset = 0;
		size_t _endOffset = 0;

		uint32_t header[4]; //33, 33, 4, 2
		uint32_t hash0;
		uint32_t unknown0;
		uint32_t hash1;
		std::string timeUnit;
		uint32_t hash2;		//editstatecomp
		uint32_t unknown1;
		float unknown2;		//160
		std::string skybox;
		std::string introLvl;

		std::vector<SequinMasterLvl> sublevels;

		//footer
		uint8_t footer1;	//False
		uint8_t footer2;	//True
		uint32_t footer3;	//3
		uint32_t footer4;	//50
		uint32_t footer5;	//8
		uint32_t footer6;	//15
		float footer7;		//0.6f
		float footer8;		//0.5f
		float footer9;		//0.5f
		std::string checkpointLvl;
		std::string pathGameplay;	//I haven't seen this change, it's always "path.gameplay"

		void deserialize(ByteStream& aStream);
	};

	struct Lvl final {						// struct developed from 0x5177 offset from demo.objlib
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
		uint32_t unknown4;
		uint8_t unknown5;

		// One of the rare cases where the size of the array isnt first, only way we currently know about this is via a continuation byte
		std::vector<LvlLeafSequin> leafSequin;


		
		uint8_t unknownBool5;

		float unknownFloat12;
		std::string flowref;
		std::vector<TraitSelector> traitSelectors;
		std::string kNumTraitType;	//available options are		kNumTraitInterps	kNumTraitTypes
		uint8_t unknownBool6;
		std::string tutorialType; // See: is_known_tutorial_type

		//footer (maybe????)

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

	struct ObjlibLevel final {
		std::vector<std::byte> _bytes;
		std::vector<Leaf> _leafs;
		std::vector<Samp> _samps;
		std::vector<Spn> _spns;
		std::vector<SequinMaster> _masters;
		std::vector<SequinDrawer> _drawers;
		std::vector<Lvl> _lvls;
		std::vector<Gate> _gates;

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