#include "te_structs.hpp"

#include <array>
#include <span>
#include <iostream>

namespace aurora {
    bool is_known_step_type(std::string_view aStepType) {
        if (aStepType == "kStepAny") return true;
        if (aStepType == "kStepFirst") return true;
        if (aStepType == "kStepGameplay") return true;
        if (aStepType == "kStepLast") return true;
        if (aStepType == "kStepProp") return true;
        return false;
    }

    bool is_known_tutorial_type(std::string_view aTutorial) {
        if (aTutorial == "TUTORIAL_GRIND") return true;
        if (aTutorial == "TUTORIAL_JUMP") return true;
        if (aTutorial == "TUTORIAL_LANES") return true;
        if (aTutorial == "TUTORIAL_NONE") return true;
        if (aTutorial == "TUTORIAL_POUND") return true;
        if (aTutorial == "TUTORIAL_POUND_REMINDER") return true;
        if (aTutorial == "TUTORIAL_POWER_GRIND") return true;
        if (aTutorial == "TUTORIAL_THUMP") return true;
        if (aTutorial == "TUTORIAL_THUMP_REMINDER") return true;
        if (aTutorial == "TUTORIAL_TURN_LEFT") return true;
        if (aTutorial == "TUTORIAL_TURN_RIGHT") return true;
        return false;
    }

	void Datapoint::deserialize(ByteStream& aStream, uint32_t aDatatype) {
		time = aStream.read_f32();

        enum Datatype : uint32_t {
            kTraitInt = 0,
            kTraitBool = 1,
            kTraitFloat = 2,
            kTraitColor = 3,
            kTraitAction = 8,
        };

        switch (aDatatype) {
        case kTraitInt:
            value = aStream.read_s32();
            break;
        case kTraitBool:
            value = aStream.read_u8();
            break;
        case kTraitFloat:
            value = aStream.read_f32();
            break;
        case kTraitColor:
            value = aStream.read_f32vec4();
            break;
        case kTraitAction:
            value = aStream.read_u8();
            break;
        default:
            __debugbreak(); // Unsupported datatype
        }

        interpolation = aStream.read_str();
        easing = aStream.read_str();
	}

    void Trait::deserialize(ByteStream& aStream) {
        object = aStream.read_str();
        selectors.resize(aStream.read_u32());

        for(auto& selector : selectors) {
            selector.selector = aStream.read_u32();
            selector.shareIdx = aStream.read_s32();
        }
        
        datatype = aStream.read_u32();

        datapoints.resize(aStream.read_u32());
        for (size_t iDatapoint = 0; iDatapoint < datapoints.size(); ++iDatapoint) {
            datapoints[iDatapoint].deserialize(aStream, datatype);
        }

        editorDatapoints.resize(aStream.read_u32());
        for (size_t iDatapoint = 0; iDatapoint < editorDatapoints.size(); ++iDatapoint) {
            editorDatapoints[iDatapoint].deserialize(aStream, datatype);
        }

        unknown1 = aStream.read_u32();
        unknown2 = aStream.read_u32();
        unknown3 = aStream.read_u32();
        unknown4 = aStream.read_u32();
        unknown5 = aStream.read_u32();

        intensity0 = aStream.read_str();
        intensity1 = aStream.read_str();

        unknown6 = aStream.read_u8();
        unknown7 = aStream.read_u8();
        unknown8 = aStream.read_u32();

        unknown9 = aStream.read_f32();
        unknown10 = aStream.read_f32();
        unknown11 = aStream.read_f32();
        unknown12 = aStream.read_f32();
        unknown13 = aStream.read_f32();

        unknown14 = aStream.read_u8();
        unknown15 = aStream.read_u8();
        unknown16 = aStream.read_u8();
    }

    void SequinLeaf::deserialize(ByteStream& aStream) {
        for (size_t i = 0; i < kHeader.size(); ++i) {
            assert(header[i] = aStream.read_u32() == kHeader[i]);
        }

        hash0 = aStream.read_u32();
        unknown0 = aStream.read_u32();
        hash1 = aStream.read_u32();
        timeUnit = aStream.read_str();
        hash2 = aStream.read_u32();

        traits.resize(aStream.read_u32());
        for (int iTrait = 0; iTrait < traits.size(); ++iTrait) {
            traits[iTrait].deserialize(aStream);
        }

        unknown1 = aStream.read_u32();

        unknown2.resize(aStream.read_u32());
        for (int iUnknown2 = 0; iUnknown2 < unknown2.size(); ++iUnknown2) {
            unknown2[iUnknown2] = aStream.read_u32vec3();
        }

        unknown3 = aStream.read_u32();
        unknown4 = aStream.read_u32();
        unknown5 = aStream.read_u32();
    }

    void write_content(std::string origin, std::string declaredName, std::span<std::byte const> data) {
        std::filesystem::path directory = std::format("thumper/{}", origin);
        std::filesystem::create_directories(directory);
        std::filesystem::path outputPath = directory / declaredName;
        std::ofstream stream(outputPath, std::ios::binary | std::ios::out);
        stream.write(reinterpret_cast<char const*>(data.data()), data.size_bytes());
    }

    void ObjlibLevel::deserialize(ByteStream& aStream) {
        filetype = aStream.read_u32();
        assert(filetype == 0x8);
        objlibType = aStream.read_u32();
        assert(objlibType == 0x0b374d9e);

        unknown0 = aStream.read_u32();
        unknown1 = aStream.read_u32();
        unknown2 = aStream.read_u32();
        unknown3 = aStream.read_u32();

        libraryImports.resize(aStream.read_u32());
        for (int i = 0; i < libraryImports.size(); ++i) {
            libraryImports[i].unknown0 = aStream.read_u32();
            libraryImports[i].library = aStream.read_str();
        }

        origin = aStream.read_str();
        
        objectImports.resize(aStream.read_u32());
        for (int i = 0; i < objectImports.size(); ++i) {
            objectImports[i].type = aStream.read_u32();
            objectImports[i].name = aStream.read_str();
            objectImports[i].unknown0 = aStream.read_u32();
            objectImports[i].library = aStream.read_str();
        }

        objectDeclarations.resize(aStream.read_u32());
        for (int i = 0; i < objectDeclarations.size(); ++i) {
            objectDeclarations[i].type = static_cast<DeclarationType>(aStream.read_u32());
            objectDeclarations[i].name = aStream.read_str();
        }

        // Begin object readback

        for (size_t iDeclaration = 0; iDeclaration < objectDeclarations.size(); ++iDeclaration) {
            auto& declaration = objectDeclarations[iDeclaration];

            if (declaration.type == DeclarationType::kLeaf) {
                auto headerBytes = std::as_bytes(std::span(SequinLeaf::kHeader));

                auto it = std::search(aStream.mData.begin() + aStream.mOffset, aStream.mData.end(), headerBytes.begin(), headerBytes.end());
                if (it != aStream.mData.end()) {
                    auto jump = std::distance(aStream.mData.begin() + aStream.mOffset, it);
                    // if (jump != 0) __debugbreak(); // Non zero jump, validate contents
                    aStream.advance(jump);

                    declaration._definitionOffset = aStream.mOffset;
                    SequinLeaf definition;
                    definition._declaredName = declaration.name;
                    definition._beginOffset = aStream.mOffset;
                    definition.deserialize(aStream);
                    definition._endOffset = aStream.mOffset;

                    write_content(origin, definition._declaredName, std::span<std::byte const>(aStream.mData.data() + definition._beginOffset, definition._endOffset - definition._beginOffset));
                    _leafs.push_back(std::move(definition));

                }
            }
            else if (declaration.type == DeclarationType::kSamp) {
                auto headerBytes = std::as_bytes(std::span(Sample::kHeader));
            
                auto it = std::search(aStream.mData.begin() + aStream.mOffset, aStream.mData.end(), headerBytes.begin(), headerBytes.end());
                if (it != aStream.mData.end()) {
                    aStream.advance(std::distance(aStream.mData.begin() + aStream.mOffset, it));
            
                    declaration._definitionOffset = aStream.mOffset;
                    Sample definition;
                    definition._declaredName = declaration.name;
                    definition._beginOffset = aStream.mOffset;
                    definition.deserialize(aStream);
                    definition._endOffset = aStream.mOffset;
            
                    write_content(origin, definition._declaredName, std::span<std::byte const>(aStream.mData.data() + definition._beginOffset, definition._endOffset - definition._beginOffset));
                    _samps.push_back(std::move(definition));
                }
                
            }
            else if (declaration.type == DeclarationType::kSpn) {
                auto headerBytes = std::as_bytes(std::span(EntitySpawner::kHeader));
            
                auto it = std::search(aStream.mData.begin() + aStream.mOffset, aStream.mData.end(), headerBytes.begin(), headerBytes.end());
                if (it != aStream.mData.end()) {
                    aStream.advance(std::distance(aStream.mData.begin() + aStream.mOffset, it));
            
                    declaration._definitionOffset = aStream.mOffset;
                    EntitySpawner definition;
                    definition._declaredName = declaration.name;
                    definition._beginOffset = aStream.mOffset;
                    definition.deserialize(aStream);
                    definition._endOffset = aStream.mOffset;
            
                    write_content(origin, definition._declaredName, std::span<std::byte const>(aStream.mData.data() + definition._beginOffset, definition._endOffset - definition._beginOffset));
                    _spns.push_back(std::move(definition));
                }
            }
            else if (declaration.type == DeclarationType::kMaster) {
                std::span<std::byte const> headerBytes = std::as_bytes(std::span(SequinMaster::kHeader));
            
                auto it = std::search(aStream.mData.begin() + aStream.mOffset, aStream.mData.end(), headerBytes.begin(), headerBytes.end());
                if (it != aStream.mData.end()) {
                    aStream.advance(std::distance(aStream.mData.begin() + aStream.mOffset, it));
            
                    declaration._definitionOffset = aStream.mOffset;
                    SequinMaster definition;
                    definition._declaredName = declaration.name;
                    definition._beginOffset = aStream.mOffset;
                    definition.deserialize(aStream);
                    definition._endOffset = aStream.mOffset;
            
                    write_content(origin, definition._declaredName, std::span<std::byte const>(aStream.mData.data() + definition._beginOffset, definition._endOffset - definition._beginOffset));
                    _masters.push_back(std::move(definition));
                }
            }
            else if (declaration.type == DeclarationType::kDrawer) {
                std::span<std::byte const> headerBytes = std::as_bytes(std::span(SequinDrawer::kHeader));
            
                auto it = std::search(aStream.mData.begin() + aStream.mOffset, aStream.mData.end(), headerBytes.begin(), headerBytes.end());
                if (it != aStream.mData.end()) {
                    aStream.advance(std::distance(aStream.mData.begin() + aStream.mOffset, it));
            
                    declaration._definitionOffset = aStream.mOffset;
                    SequinDrawer definition;
                    definition._declaredName = declaration.name;
                    definition._beginOffset = aStream.mOffset;
                    definition.deserialize(aStream);
                    definition._endOffset = aStream.mOffset;
            
                    write_content(origin, definition._declaredName, std::span<std::byte const>(aStream.mData.data() + definition._beginOffset, definition._endOffset - definition._beginOffset));
                    _drawers.push_back(std::move(definition));
                }
            
            }
            else if (declaration.type == DeclarationType::kGate) {
                std::span<std::byte const> headerBytes = std::as_bytes(std::span(SequinGate::kHeader));
            
                auto it = std::search(aStream.mData.begin() + aStream.mOffset, aStream.mData.end(), headerBytes.begin(), headerBytes.end());
                if (it != aStream.mData.end()) {
                    aStream.advance(std::distance(aStream.mData.begin() + aStream.mOffset, it));
            
                    declaration._definitionOffset = aStream.mOffset;
                    SequinGate definition;
                    definition._declaredName = declaration.name;
                    definition._beginOffset = aStream.mOffset;
                    definition.deserialize(aStream);
                    definition._endOffset = aStream.mOffset;
            
                    write_content(origin, definition._declaredName, std::span<std::byte const>(aStream.mData.data() + definition._beginOffset, definition._endOffset - definition._beginOffset));
                    _gates.push_back(std::move(definition));
                }
            
            }
            else if (declaration.type == DeclarationType::kLvl) {
                std::span<std::byte const> headerBytes = std::as_bytes(std::span(SequinLevel::kHeader));
            
                auto it = std::search(aStream.mData.begin() + aStream.mOffset, aStream.mData.end(), headerBytes.begin(), headerBytes.end());
                if (it != aStream.mData.end()) {
                    aStream.advance(std::distance(aStream.mData.begin() + aStream.mOffset, it));
            
                    declaration._definitionOffset = aStream.mOffset;
                    SequinLevel definition;
                    definition._declaredName = declaration.name;
                    definition._beginOffset = aStream.mOffset;
                    definition.deserialize(aStream);
                    definition._endOffset = aStream.mOffset;
            
                    write_content(origin, definition._declaredName, std::span<std::byte const>(aStream.mData.data() + definition._beginOffset, definition._endOffset - definition._beginOffset));
                    _lvls.push_back(std::move(definition));
                }
            
            }

            else if (declaration.type == DeclarationType::kPath) {
                std::span<std::byte const> headerBytes = std::as_bytes(std::span(Path::kHeader));
            
                auto it = std::search(aStream.mData.begin() + aStream.mOffset, aStream.mData.end(), headerBytes.begin(), headerBytes.end());
                if (it != aStream.mData.end()) {
                    aStream.advance(std::distance(aStream.mData.begin() + aStream.mOffset, it));
            
                    declaration._definitionOffset = aStream.mOffset;
                    Path definition;
                    definition._declaredName = declaration.name;
                    definition._beginOffset = aStream.mOffset;
                    definition.deserialize(aStream);
                    definition._endOffset = aStream.mOffset;
            
                    write_content(origin, definition._declaredName, std::span<std::byte const>(aStream.mData.data() + definition._beginOffset, definition._endOffset - definition._beginOffset));
                    _paths.push_back(std::move(definition));
                }
            
            }
        }


#if 0
        // If last object was found we can happily read the footer
        if (objectDeclarations.back()._definitionOffset != 0) {
            assert(aStream.read_u32() == 0);
            assert(aStream.read_u32() == 4);

            struct Camera final {
                // Matches drawer??
                uint32_t hash0;
                uint32_t unknown0; // 8
                bool unknown1;
                std::string drawLayers; // kNumDrawLayers
                std::string bucketParent; // kBucketParent
                std::vector<std::string> strings;
                // END
                uint32_t unknown2;
                uint32_t unknown3;
                std::string xfmer;
                std::string constraint; // kConstraintParent
                Transform transform;

                void deserialize(ByteStream& aStream) {
                    hash0 = aStream.read_u32();
                    unknown0 = aStream.read_u32();
                    unknown1 = aStream.read_u8();
                    drawLayers = aStream.read_str();
                    bucketParent = aStream.read_str();
                    strings.resize(aStream.read_u32());

                    for (size_t i = 0; i < strings.size(); ++i) {
                        strings[i] = aStream.read_str();
                    }

                    unknown2 = aStream.read_u32();
                    unknown3 = aStream.read_u32();
                    xfmer = aStream.read_str();
                    constraint = aStream.read_str(); // kConstraintParent
                    transform = aStream.read_transform();
                }
           };
          
            uint32_t unknown0 = aStream.read_u32();
            uint32_t unknown1 = aStream.read_u32();
            uint32_t unknown2 = aStream.read_u32();
            Camera unknownCameraDefinition0;
            unknownCameraDefinition0.deserialize(aStream);

            auto camera = aStream.read_str(); // world.cam

            uint32_t unknown3 = aStream.read_u32();
            uint32_t unknown4 = aStream.read_u32();
            uint32_t unknown5 = aStream.read_u32();
            Camera unknownCameraDefinition1;
            unknownCameraDefinition1.deserialize(aStream);

            auto scaling = aStream.read_f32vec3();

            sceneName = aStream.read_str();
            lowSpecScene = aStream.read_str();
            vrSettings = aStream.read_str();
            environment = aStream.read_str();
            playerCamera = aStream.read_str();
            unknownfooteritem = aStream.read_str();
            playerCam2 = aStream.read_str();
            bpm = aStream.read_f32();
            avatar = aStream.read_str();
            master = aStream.read_str();
            drawer = aStream.read_str();
            masterChannel = aStream.read_str();
            baseChannel = aStream.read_str();
            realtimeChannel = aStream.read_str();
            unknownFooterval = aStream.read_u8();
        }
#endif
       

        // Footer
    }

    void Sample::deserialize(ByteStream& aStream) {
        for (size_t i = 0; i < kHeader.size(); ++i) {
            assert(header[i] = aStream.read_u32() == kHeader[i]);
        }

        // if 0x01 the next value is a hash, if 0 there is no hash
        // The datatype doesnt match a thumper boolean with a 1 byte size
        // This may likely be a hash count, but we've only observed values of 0 and 1
        // and thus are assuming this is a boolean
        // Theres only a handful of cases where this is 0 and is typically 1
        uint32_t hasHash = aStream.read_u32();
        if (hasHash == 0x01) hash0 = aStream.read_u32();
        assert(hasHash < 2); // If this is ever >= 2, we want to know about it

        samplePlayMode = aStream.read_str();
        unknown0 = aStream.read_u32();
        filePath = aStream.read_str();

        for (int i = 0; i < 5; ++i) {
            unknown1[i] = aStream.read_u8();
        }

        volume = aStream.read_f32();
        pitch = aStream.read_f32();
        pan = aStream.read_f32();
        offset = aStream.read_f32();
        channelGroup = aStream.read_str();
    }

    void EntitySpawner::deserialize(ByteStream& aStream) {
        for (size_t i = 0; i < kHeader.size(); ++i) {
            assert(header[i] = aStream.read_u32() == kHeader[i]);
        }

        hash0 = aStream.read_u32();
        hash1 = aStream.read_u32();
        unknown0 = aStream.read_u32();
        xfmName = aStream.read_str();

        constraint = aStream.read_str();

        transform = aStream.read_transform();

        unknown2 = aStream.read_u32();

        objlibPath = aStream.read_str();
        bucketType = aStream.read_str();
    }

    void SequinMasterLvl::deserialize(ByteStream& aStream) {
        lvlName = aStream.read_str();
        gateName = aStream.read_str();
        isCheckpoint = aStream.read_u8();
        checkpointLeaderLvlName = aStream.read_str();
        restLvlName = aStream.read_str();

        unknownBool0 = aStream.read_u8();
        unknownBool1 = aStream.read_u8();
        unknown0 = aStream.read_u32();
        unknownBool2 = aStream.read_u8();

        playPlus = aStream.read_u8();
    }

    void SequinMaster::deserialize(ByteStream& aStream) {
        for (size_t i = 0; i < kHeader.size(); ++i) {
            assert(header[i] = aStream.read_u32() == kHeader[i]);
        }

        hash0 = aStream.read_u32();
        unknown0 = aStream.read_u32();
        hash1 = aStream.read_u32();
        timeUnit = aStream.read_str();
        hash2 = aStream.read_u32();
        unknown1 = aStream.read_u32();
        unknown2 = aStream.read_f32();
        skybox = aStream.read_str();
        introLvl = aStream.read_str();

        sublevels.resize(aStream.read_u32());

        for (auto& sublevel : sublevels) {
            sublevel.deserialize(aStream);
        }
        
        footer1 = aStream.read_u8();
        footer2 = aStream.read_u8();
        footer3 = aStream.read_u32();
        footer4 = aStream.read_u32();
        footer5 = aStream.read_u32();
        footer6 = aStream.read_u32();
        footer7 = aStream.read_f32();
        footer8 = aStream.read_f32();
        footer9 = aStream.read_f32();
        checkpointLvl = aStream.read_str();
        pathGameplay = aStream.read_str();
    }

    void SequinDrawer::deserialize(ByteStream& aStream) {
        for (size_t i = 0; i < kHeader.size(); ++i) {
            assert(header[i] = aStream.read_u32() == kHeader[i]);
        }

        hash0 = aStream.read_u32();
        unknown0 = aStream.read_u32();
        unknownBool0 = aStream.read_u8();
        drawLayers = aStream.read_str();
        bucketType = aStream.read_str();
        unknown1 = aStream.read_u32();
    }

    

    void LvlLeafSequin::deserialize(ByteStream& aStream) {
        unknown0 = aStream.read_u32();
        beatCount = aStream.read_u32();
        unknownBool1 = aStream.read_u8();
        leafName = aStream.read_str();
        defaultPath = aStream.read_str();

        subpaths.resize(aStream.read_u32());

        for (auto& subpath : subpaths) {
            subpath.string = aStream.read_str();
            subpath.unknown2 = aStream.read_u32();
        }

        stepType = aStream.read_str();
        assert(is_known_step_type(stepType));

        unknown1 = aStream.read_u32();

        transform = aStream.read_transform();

        unknownBool2 = aStream.read_u8();
        unknownBool3 = aStream.read_u8();
        continuation = aStream.read_u8();
    }

    void SampleEntry::deserialize(ByteStream& aStream) {
        sampleName = aStream.read_str();
        loopBeats = aStream.read_u32();
        unknown2 = aStream.read_u32();
    }

    

    void SequinLevel::deserialize(ByteStream& aStream) {
        for (size_t i = 0; i < kHeader.size(); ++i) {
            assert(header[i] = aStream.read_u32() == kHeader[i]);
        }

        hash0 = aStream.read_u32();
        unknown0 = aStream.read_u32();
        hash1 = aStream.read_u32();
        timeUnit = aStream.read_str();

        unknown1 = aStream.read_u32();
        unknownInt0 = aStream.read_u32();
        editStateCompHash = aStream.read_u32();

        traits.resize(aStream.read_u32());

        for (auto& trait : traits) {
            trait.deserialize(aStream);
        }

        unknown2 = aStream.read_u32();
        phaseMoveType = aStream.read_str();
        unknown3 = aStream.read_u32();
        unknown5 = aStream.read_u8();

        while (true) {
            LvlLeafSequin sequin;
            sequin.deserialize(aStream);
            leafSequin.push_back(sequin);

            // If continuation byte is false, we are done reading this section
            if (sequin.continuation == false) break;
        }

        sampNames.resize(aStream.read_u32());

        for (auto& sample : sampNames) {
            sample.deserialize(aStream);
        }
        
        unknownBool5 = aStream.read_u8();
        unknownFloat12 = aStream.read_f32();
        flowref = aStream.read_str();

        traitSelectors.resize(aStream.read_u32());

        for (auto& selector : traitSelectors) {
            selector.selector = aStream.read_u32();
            selector.shareIdx = aStream.read_u32();
        }


        kNumTraitType = aStream.read_str();
        unknownBool6 = aStream.read_u8();
        tutorialType = aStream.read_str();

        assert(is_known_tutorial_type(tutorialType));

        footer1 = aStream.read_f32();
        footer2 = aStream.read_f32();
        footer3 = aStream.read_f32();
    }

    void SequinGate::GateEntry::deserialize(ByteStream& aStream) {
        bucketHash = aStream.read_u32();
        lvlName = aStream.read_str();
        unknown1 = aStream.read_u8();
        sentryType = aStream.read_str();
        hash = aStream.read_u32();
        unknowncounter = aStream.read_u32();
    }

    void SequinGate::deserialize(ByteStream& aStream) {
        for (size_t i = 0; i < kHeader.size(); ++i) {
            assert(header[i] = aStream.read_u32() == kHeader[i]);
        }
        
        editStateComp = aStream.read_u32();
        spn = aStream.read_str();
        unknown0 = aStream.read_u32();
        spnParameter = aStream.read_u32();
        unknown1 = aStream.read_s32();

        enteries.resize(aStream.read_u32());

        for (int i = 0; i < enteries.size(); ++i) {
            enteries[i].deserialize(aStream);
        }

        preintro = aStream.read_str();
        postintro = aStream.read_str();
        restart = aStream.read_str();
        unknownLvlParam = aStream.read_str();
        sectionBossType = aStream.read_str();
        unknown2 = aStream.read_f32();
        randomFunction = aStream.read_str();
    }

    void Path::deserialize(ByteStream& aStream) {
        for (size_t i = 0; i < kHeader.size(); ++i) {
            assert(header[i] = aStream.read_u32() == kHeader[i]);
        }

        unknown0 = aStream.read_u32();
        assert(unknown0 < 2);

        hash0 = aStream.read_u32();

        scale0 = aStream.read_f32vec3();
        scale1 = aStream.read_f32vec3();

        if (unknown0 == 1) {
            unknown6 = aStream.read_u32();
        }

        if (unknown0 == 0) {

            __debugbreak();
        }

        meshName = aStream.read_str();

        

        unknownBool0 = aStream.read_u8();
        pathInterpType = aStream.read_str();
        unknown7 = aStream.read_u32();
        unknown8 = aStream.read_u8();
        unknown9 = aStream.read_u8();
        decorators.resize(aStream.read_u32());
        for (auto i = 0; i < decorators.size(); i++)
        {
            decorators[i] = aStream.read_str();
        }
        unknownBool1 = aStream.read_u8();
    }
}