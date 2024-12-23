#ifdef TE_WINDOWS
#   include <Windows.h>
#endif

#define AURORA_NAME "Aurora"
#define AURORA_BASE_VERSION "v0.0.4-a.1"
const char* kLinkDiscord = "https://discord.com/invite/gTQbquY";
char const* kLinkGithub = "https://github.com/anthofoxo/aurora";

#ifdef TE_DEBUG
#   define AURORA_VERSION AURORA_BASE_VERSION "+" __DATE__ " " __TIME__
#else
#   define AURORA_VERSION AURORA_BASE_VERSION
#endif
#define AURORA_NAME_VERSION AURORA_NAME " " AURORA_VERSION

#include <glad/gl.h>
#include <GLFW/glfw3.h>

#include "te_hash.hpp"
#include "te_audio.hpp"
#include "te_window.hpp"
#include "te_image.hpp"
#include "te_diff_table.hpp"
#include "te_structs.hpp"

#include <stb_image.h>
#include <miniaudio.h>

#include <tinyfiledialogs.h>
#include <yaml-cpp/yaml.h>

#include <imgui.h>
#include <imgui_internal.h>
#include <backends/imgui_impl_glfw.h>
#include <backends/imgui_impl_opengl3.h>
#include <misc/cpp/imgui_stdlib.h>
#include <imgui_memory_editor.h>

#include <iostream>
#include <cstdlib> // EXIT_SUCCESS, EXIT_FAILURE
#include <stdio.h>
#include <string>
#include <vector>
#include <filesystem>
#include <fstream>
#include <memory>

struct Level {
    std::string name;
    std::string difficulty;
    std::string description;
    std::string author;
};


void hash_panel(bool& open) {
    static std::string input = "type input here";
    static uint32_t hash = aurora::hash(input);

    static std::string revinput = "type hash here";
    static std::string revresult = "<unknown>";

    if (!open) return;

    if (ImGui::Begin("Hash Panel", &open)) {
        ImGui::SeparatorText("Hasher");
        if (ImGui::InputText("Input", &input)) hash = aurora::hash(input);
        ImGui::Text("0x%02x", hash);

        ImGui::SeparatorText("Hash Lookup");
        if (ImGui::InputText("Lookup Hash", &revinput)) {
            try {
                uint32_t input = std::stoull(revinput, 0, 16);
                revresult = aurora::rev_hash(input);
            }
            catch (std::exception const&) {
                revresult = "<invalid input>";
            }
        }

        ImGui::LabelText("Output", "%s", revresult.c_str());
    }
    ImGui::End();
}

void about_panel(GLuint icon, bool& open) {
    if (!open) return;

    if (open) {
        if (ImGui::Begin("About " AURORA_NAME, &open, ImGuiWindowFlags_AlwaysAutoResize)) {
            ImGui::Image((ImTextureID)(uintptr_t)icon, { 128.0f, 128.0f });
            ImGui::TextUnformatted("Version " AURORA_VERSION);
            ImGui::TextUnformatted("Copyright (c) 2024 AnthoFoxo, CocoaMix, JLMusic");
            ImGui::TextUnformatted("A tool used for building custom level for the game Thumper.");
        }
        ImGui::End();
    }
}

std::string path_to_string(std::filesystem::path const& path) {
    auto const u8string = path.generic_u8string();
    return std::string(reinterpret_cast<char const*>(u8string.data()), u8string.cend() - u8string.cbegin());
}

std::optional<std::filesystem::path> select_directory_from_file() {
    char const* filter = "THUMPER_*.exe";
    char* selection = tinyfd_openFileDialog("Select your Thumper Installation Directory", nullptr, 1, &filter, nullptr, false);
    if (!selection) return std::nullopt;
    return std::filesystem::path(selection).parent_path();
}

std::optional<std::filesystem::path> select_directory_save() {
    auto path = select_directory_from_file();

    if (path) {
        YAML::Node rootNode = YAML::Node();
        rootNode["path"] = path_to_string(path.value());

        std::ofstream file("config.yaml", std::ios::out | std::ios::binary);
        file << rootNode;
    }

    return path;
}

void imgui_init(GLFWwindow* window) {
    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImGuiIO& io = ImGui::GetIO();
    io.ConfigFlags |= ImGuiConfigFlags_NavEnableKeyboard;
    io.ConfigFlags |= ImGuiConfigFlags_NavEnableGamepad;
    io.ConfigFlags |= ImGuiConfigFlags_DockingEnable;
    // io.ConfigFlags |= ImGuiConfigFlags_ViewportsEnable;

    ImGui::StyleColorsDark();

    ImGuiStyle& style = ImGui::GetStyle();
    if (io.ConfigFlags & ImGuiConfigFlags_ViewportsEnable) {
        style.WindowRounding = 0.0f;
        style.Colors[ImGuiCol_WindowBg].w = 1.0f;
    }

    ImGui_ImplGlfw_InitForOpenGL(window, true);
    ImGui_ImplOpenGL3_Init("#version 150");

    io.Fonts->AddFontFromFileTTF("GlacialIndifference-Regular.otf", 18.0f);
}

void imgui_uninit() {
    ImGui_ImplOpenGL3_Shutdown();
    ImGui_ImplGlfw_Shutdown();
    ImGui::DestroyContext();
}

std::vector<aurora::ObjlibLevel> gLevels;

void read_all_leafs(std::optional<std::filesystem::path> const& aThumperPath) {
    std::string_view paths[] = {
        "Alevels/title_screen.objlib",
        "Alevels/demo.objlib",
        "Alevels/level2/level_2a.objlib",
        "Alevels/level3/level_3a.objlib",
        "Alevels/level4/level_4a.objlib",
        "Alevels/level5/level_5a.objlib",
        "Alevels/level6/level_6.objlib",
        "Alevels/level7/level_7a.objlib",
        "Alevels/level8/level_8a.objlib",
        "Alevels/level9/level_9a.objlib",
    };

    for (auto const& pathUnhashed : paths) {
        std::string path = std::format("{}/cache/{:x}.pc", path_to_string(aThumperPath.value()), aurora::hash(pathUnhashed));

        if (!std::filesystem::exists(path)) {
            std::cout << "File " << pathUnhashed << " (" << path << ") doesnt exist.\n";
            continue;
        }

        aurora::ByteStream stream = aurora::ByteStream(path);
        aurora::ObjlibLevel level;
        level.deserialize(stream);
        level._bytes = std::move(stream.mData);

        gLevels.push_back(std::move(level));
    }
}

class Application final {
public:
    void init();
    void uninit();
    void run();
    void update();
public:
    aurora::Window mWindow;
    aurora::AudioEngine mAudioEngine;
    bool mRunning = true;
    ImFont* mMonoFont = nullptr;

    GLuint mIconTexture = 0;
    std::array<GLuint, 8> mDiffTextures{};

    std::optional<std::filesystem::path> mThumperPath;

    bool mShowHashPanel = false;
    bool mShowAboutPanel = false;
    bool mShowDifficultyExplanation = false;
    bool mModMode = false;
    bool mShowDearImGuiDemo = false;
    bool mLeafEditorShown = false;
    bool mLvlEditorShown = false;
    bool mGateEditorShown = false;
    bool mMasterEditorShown = false;
    bool mWorkingFolderShown = false;
    bool mSampleEditorShown = false;
    bool mViewHexEditor = false;
    bool mViewDebugInfo = false;

    std::vector<Level> mLevels;

    struct ViewingContext final {
        aurora::Leaf* pLeaf = nullptr;
        std::string leafTitle;

        aurora::Samp* pSample = nullptr;
        std::string sampleTitle;

        aurora::Spn* pSpn = nullptr;
        std::string spnTitle;

        aurora::SequinMaster* pMaster = nullptr;
        std::string masterTitle;

        aurora::SequinDrawer* pDrawer = nullptr;
        std::string drawerTitle;

        MemoryEditor memoryEditor;
        void* memoryEditorOffset = nullptr;
        size_t memoryEditorSize = 0;
    };

    ViewingContext mContext;

    void gui_memory_editor() {
        if (!mViewHexEditor) return;

        if (!ImGui::Begin("Hex Editor", &mViewHexEditor)) {
            ImGui::End();
            return;
        }
        ImGui::PushFont(mMonoFont);
        mContext.memoryEditor.DrawContents(mContext.memoryEditorOffset, mContext.memoryEditorSize, 0);
        ImGui::PopFont();
        ImGui::End();
    }

    void gui_sample_viewer() {
        if (!mContext.sampleTitle.empty() && mContext.pSample) {
            if (ImGui::Begin(mContext.sampleTitle.c_str())) {
                ImGui::LabelText("Play Mode", "%s", mContext.pSample->samplePlayMode.c_str());
                ImGui::LabelText("Path", "%s", mContext.pSample->filePath.c_str());
                ImGui::LabelText("Volume", "%.2f", mContext.pSample->volume);
                ImGui::LabelText("Pitch", "%.2f", mContext.pSample->pitch);
                ImGui::LabelText("Pan", "%.2f", mContext.pSample->pan);
                ImGui::LabelText("Offset", "%.2f", mContext.pSample->offset);
                ImGui::LabelText("Channel Group", "%s", mContext.pSample->channelGroup.c_str());
                
            }
            ImGui::End();
        }
    }

    void gui_master_viewer() {
        if (!mContext.masterTitle.empty() && mContext.pMaster) {
            if (ImGui::Begin(mContext.masterTitle.c_str())) {
                ImGui::LabelText("Time Unit", "%s", mContext.pMaster->timeUnit.c_str());
                ImGui::LabelText("Unknown2", "%f", mContext.pMaster->unknown2);
                ImGui::LabelText("Skybox", "%s", mContext.pMaster->skybox.c_str());
                ImGui::LabelText("Intro Level", "%s", mContext.pMaster->introLvl.c_str());
                ImGui::LabelText("Checkpoint Level", "%s", mContext.pMaster->checkpointLvl.c_str());
                ImGui::LabelText("Path", "%s", mContext.pMaster->pathGameplay.c_str());

                for (auto& sublevel : mContext.pMaster->sublevels) {
                    ImGui::Separator();
                    ImGui::LabelText("Level name", "%s", sublevel.lvlName.c_str());
                    ImGui::LabelText("Gate name", "%s", sublevel.gateName.c_str());
                    ImGui::LabelText("Leader level", "%s", sublevel.checkpointLeaderLvlName.c_str());
                    ImGui::LabelText("Rest level", "%s", sublevel.restLvlName.c_str());

                    ImGui::Checkbox("Has checkpoint", &sublevel.isCheckpoint);
                    ImGui::Checkbox("Playplus", &sublevel.playPlus);
                }

            }
            ImGui::End();
        }
    }

    void gui_drawer_viewer() {
        if (!mContext.drawerTitle.empty() && mContext.pDrawer) {
            if (ImGui::Begin(mContext.drawerTitle.c_str())) {
                ImGui::LabelText("hash0", "0x%x", mContext.pDrawer->hash0);
                ImGui::LabelText("unknown0", "%u", mContext.pDrawer->unknown0);
                ImGui::LabelText("unknownBool0", "%d", mContext.pDrawer->unknownBool0);

                ImGui::LabelText("Draw Layers", "%s", mContext.pDrawer->drawLayers.c_str());
                ImGui::LabelText("Bucket Type", "%s", mContext.pDrawer->bucketType.c_str());
                ImGui::LabelText("unknown1", "%u", mContext.pDrawer->unknown1);
            }
            ImGui::End();
        }
    }

    void gui_leaf_viewer() {
        if (!mContext.leafTitle.empty() && mContext.pLeaf) {
            if (ImGui::Begin(mContext.leafTitle.c_str())) {
                int maxDataPoints = 0;

                for (auto& trait : mContext.pLeaf->traits) {
                    if (trait.datapoints.size() > maxDataPoints) maxDataPoints = trait.datapoints.size();
                }

                ImGui::Button("Hover for leaf dump detail");

                ImGui::SetItemTooltip(
                    "Time unit: %s\n"
                    "hash0: %u\n"
                    "hash1: %u\n"
                    "hash2: %u\n"
                    "Unknown 0: %d\n"
                    "Unknown 1: %d\n"
                    "Unknown 3: %d\n"
                    "Unknown 4: %d\n"
                    "Unknown 5: %d\n",
                    mContext.pLeaf->timeUnit.c_str(),
                    mContext.pLeaf->hash0,
                    mContext.pLeaf->hash1,
                    mContext.pLeaf->hash2,
                    mContext.pLeaf->unknown0,
                    mContext.pLeaf->unknown1,
                    mContext.pLeaf->unknown3,
                    mContext.pLeaf->unknown4,
                    mContext.pLeaf->unknown5
                );

                if (ImGui::BeginTable(mContext.pLeaf->_declaredName.c_str(), maxDataPoints + 1, ImGuiTableFlags_Borders | ImGuiTableFlags_SizingFixedFit | ImGuiTableFlags_ScrollX)) {
                    ImGui::TableSetupScrollFreeze(1, 1);

                    ImGui::TableNextRow();
                    ImGui::TableNextColumn();
                    ImGui::TableHeader("Object:Selector");

                    for (int i = 0; i < maxDataPoints; ++i) {
                        std::string s = std::to_string(i);
                        ImGui::TableNextColumn();
                        ImGui::TableHeader(s.c_str());
                    }

                    for (auto& trait : mContext.pLeaf->traits) {


                        ImGui::TableNextRow();
                        ImGui::TableNextColumn();

                        std::string name;

                        if (trait.object == mContext.pLeaf->_declaredName)
                            name = aurora::rev_hash(trait.selector);
                        else
                            name = std::format("{}:{}", trait.object, aurora::rev_hash(trait.selector).c_str());

                        ImGui::TableHeader(name.c_str());

                        ImGui::SetItemTooltip(
                            "intensity0: %s\n"
                            "intensity1: %s\n"
                            "Selector Share: %d\n"
                            "Unknown 0: %d\n"
                            "Unknown 1: %d\n"
                            "Unknown 2: %d\n"
                            "Unknown 3: %d\n"
                            "Unknown 4: %d\n"
                            "Unknown 5: %d\n"
                            "Unknown 6: %d\n"
                            "Unknown 7: %d\n"
                            "Unknown 8: %d\n"
                            "Unknown 9: %.2f\n"
                            "Unknown 10: %.2f\n"
                            "Unknown 11: %.2f\n"
                            "Unknown 12: %.2f\n"
                            "Unknown 13: %.2f\n"
                            "Unknown 14: %d\n"
                            "Unknown 15: %d\n"
                            "Unknown 16: %d\n",
                            trait.intensity0.c_str(),
                            trait.intensity1.c_str(),
                            trait.selectorShareIdx,
                            trait.unknown0,
                            trait.unknown1,
                            trait.unknown2,
                            trait.unknown3,
                            trait.unknown4,
                            trait.unknown5,
                            trait.unknown6,
                            trait.unknown7,
                            trait.unknown8,
                            trait.unknown9,
                            trait.unknown10,
                            trait.unknown11,
                            trait.unknown12,
                            trait.unknown13,
                            trait.unknown14,
                            trait.unknown15,
                            trait.unknown16
                        );

                        int idx = 0;
                        for (int i = 0; i < maxDataPoints; ++i) {
                            ImGui::TableNextColumn();

                            if (idx >= trait.datapoints.size()) continue;

                            auto& datapoint = trait.datapoints[idx];
                            if (std::abs(datapoint.time - static_cast<float>(i)) < 0.1f) {
                                ++idx;

                                int editorDatapointIdx = -1;

                                for (int iEditorData = 0; iEditorData < trait.editorDatapoints.size(); iEditorData += 2) {
                                    auto& start = trait.editorDatapoints[iEditorData];
                                    auto& end = trait.editorDatapoints[iEditorData + 1];

                                    if (datapoint.time >= start.time && datapoint.time <= end.time) {

                                        editorDatapointIdx = iEditorData;
                                        ImGui::TableSetBgColor(ImGuiTableBgTarget_CellBg, 0xFF003F00);

                                        break;
                                    }
                                }




                                if (trait.datatype == 2) {
                                    float val = std::any_cast<float>(datapoint.value);
                                    ImGui::PushStyleColor(ImGuiCol_Text, std::abs(val) > 0.01f ? ImVec4{ 1.0f, 1.0f, 1.0f, 1.0f } : ImVec4{ 0.5f, 0.5f, 0.5f, 1.0f });
                                    ImGui::Text("%.2f", std::any_cast<float>(datapoint.value));
                                    ImGui::PopStyleColor();
                                }
                                else if (trait.datatype == 8 || trait.datatype == 1) {
                                    bool val = std::any_cast<uint8_t>(datapoint.value);
                                    ImGui::PushStyleColor(ImGuiCol_Text, val ? ImVec4{ 1.0f, 1.0f, 1.0f, 1.0f } : ImVec4{ 0.5f, 0.5f, 0.5f, 1.0f });
                                    ImGui::Text("%s", val ? "true" : "false");
                                    ImGui::PopStyleColor();
                                }
                                else
                                    ImGui::TextUnformatted("?");



                                std::string str = std::format("Relative Offset: {:.1f}\nInterpolation: {}\nEasing: {}", datapoint.time - static_cast<float>(i), datapoint.interpolation, datapoint.easing);

                                ImGui::SetItemTooltip("%s", str.c_str());
                            }
                            else {
                                // Empty
                            }
                        }
                    }

                    ImGui::EndTable();
                }
            }
            ImGui::End();
        }
    }

    void gui_spn_viewer() {
        if (!mContext.spnTitle.empty() && mContext.pSpn) {
            if (ImGui::Begin(mContext.spnTitle.c_str())) {
                ImGui::LabelText("XFM Name", "%s", mContext.pSpn->xfmName.c_str());
                ImGui::LabelText("Constraint", "%s", mContext.pSpn->constraint.c_str());
                ImGui::LabelText("Objlib Path", "%s", mContext.pSpn->objlibPath.c_str());
                ImGui::LabelText("Bucket", "%s", mContext.pSpn->bucketType.c_str());
            }
            ImGui::End();
        }
    }
};

void Application::init() {
    // Read configs
    try {
        YAML::Node rootNode = YAML::LoadFile("config.yaml");
        std::string str = rootNode["path"].as<std::string>("");

        if (!str.empty()) {
            mThumperPath = std::filesystem::path(str);
        }
    }
    catch (YAML::BadFile const&) {
        // Do nothing, use defaults
        tinyfd_messageBox("ERROR", "Config file not found! \nYou will need to re-select your Thumper executable.", "ok", "error", 1);
    }

    // If invalid, request path
    if (!mThumperPath) {
        mThumperPath = select_directory_save();

        if (!mThumperPath) {
            mRunning = false;
            return;
        }
    }

    mWindow = aurora::Window({
        .width = 1280,
        .height = 720,
        .title = AURORA_NAME_VERSION,

        .maximized = true,
        .visible = false,
    });

    // Set window icon
    {
        aurora::Image icon32("thumper_modding_tool_32.png");
        GLFWimage image{
            .width = icon32.width(),
            .height = icon32.height(),
            .pixels = icon32.pixels(),
        };

        glfwSetWindowIcon(mWindow, 1, &image);
    }

    glfwShowWindow(mWindow);

    glfwMakeContextCurrent(mWindow);

    gladLoadGL(&glfwGetProcAddress);

    mAudioEngine.init();
    ma_engine_play_sound(&mAudioEngine.mEngine, "UIBoot.ogg", nullptr);

    // Load larger icon, seen in the about panel
    {
        aurora::Image image("thumper_modding_tool.png");
        glCreateTextures(GL_TEXTURE_2D, 1, &mIconTexture);
        glTextureStorage2D(mIconTexture, 1, GL_RGBA8, image.width(), image.height());
        glTextureSubImage2D(mIconTexture, 0, 0, 0, image.width(), image.height(), GL_RGBA, GL_UNSIGNED_BYTE, image.pixels());
        glTextureParameteri(mIconTexture, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    }

    // Load difficulty texture
    for (int i = 0; i < mDiffTextures.size(); ++i) {
        std::string path = std::format("difficulty_icons/d{}.png", i);

        aurora::Image image(path.c_str());
        glCreateTextures(GL_TEXTURE_2D, 1, &mDiffTextures[i]);
        glTextureStorage2D(mDiffTextures[i], 1, GL_RGBA8, image.width(), image.height());
        glTextureSubImage2D(mDiffTextures[i], 0, 0, 0, image.width(), image.height(), GL_RGBA, GL_UNSIGNED_BYTE, image.pixels());
        glTextureParameteri(mDiffTextures[i], GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    }

    imgui_init(mWindow);
    mMonoFont = ImGui::GetIO().Fonts->AddFontFromFileTTF("fonts/NotoSansMono-Regular.ttf", 18.0f);

    for (auto& entry : std::filesystem::directory_iterator("levels")) {
        if (!entry.is_directory()) continue;
        std::filesystem::path path = entry.path() / "LEVEL DETAILS.txt";
        if (!std::filesystem::exists(path)) continue;

        YAML::Node node = YAML::LoadFile(path_to_string(path));

        mLevels.emplace_back(
            node["level_name"].as<std::string>(""),
            node["difficulty"].as<std::string>(""),
            node["description"].as<std::string>(""),
            node["author"].as<std::string>("")
        );
    }

    aurora::reload_hashtable();
    read_all_leafs(mThumperPath);
}

void Application::uninit() {
    mRunning = false;

    for (auto& texture : mDiffTextures) {
        glDeleteTextures(1, &texture);
    }

    glDeleteTextures(1, &mIconTexture);

    imgui_uninit();

    glfwMakeContextCurrent(nullptr);

    mWindow = {};
    mAudioEngine.uninit();
}

void Application::run() {
    init();

    while (mRunning) {
        glfwPollEvents();

        if (glfwWindowShouldClose(mWindow)) mRunning = false;

#ifdef TE_WINDOWS
        ImGui::GetIO().ConfigDebugIsDebuggerPresent = ::IsDebuggerPresent();
#endif

        ImGui_ImplOpenGL3_NewFrame();
        ImGui_ImplGlfw_NewFrame();
        ImGui::NewFrame();

        ImGui::DockSpaceOverViewport();

        update();

        ImGui::Render();
        int display_w, display_h;
        glfwGetFramebufferSize(mWindow, &display_w, &display_h);
        glViewport(0, 0, display_w, display_h);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);
        ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());

        if (ImGui::GetIO().ConfigFlags & ImGuiConfigFlags_ViewportsEnable) {
            GLFWwindow* backup_current_context = glfwGetCurrentContext();
            ImGui::UpdatePlatformWindows();
            ImGui::RenderPlatformWindowsDefault();
            glfwMakeContextCurrent(backup_current_context);
        }

        glfwSwapBuffers(mWindow);
    }

    uninit();
}

void Application::update() {
    if (ImGui::BeginMainMenuBar()) {
        if (ImGui::BeginMenu("File")) {
            if (ImGui::MenuItem("Exit", ImGui::GetKeyChordName(ImGuiMod_Alt | ImGuiKey_F4))) {
                mRunning = false;
            }

            ImGui::EndMenu();
        }

        if (ImGui::BeginMenu("View")) {
            ImGui::MenuItem("Hash Panel", nullptr, &mShowHashPanel);
            ImGui::MenuItem("Hex Editor", nullptr, &mViewHexEditor);
            ImGui::MenuItem("Debug Information", nullptr, &mViewDebugInfo);
            ImGui::Separator();
            ImGui::MenuItem("Dear ImGui Demo", nullptr, &mShowDearImGuiDemo);

            ImGui::EndMenu();
        }

        if (ImGui::BeginMenu("Options")) {
            if (ImGui::MenuItem("Change Game Dir")) {
                if (auto path = select_directory_save()) mThumperPath = path;
            }

            if (ImGui::MenuItem("Reload Hashtable")) {
                aurora::reload_hashtable();
            }

            ImGui::EndMenu();
        }

        if (ImGui::BeginMenu("Help")) {
            ImGui::MenuItem("About", nullptr, &mShowAboutPanel);

            if (ImGui::MenuItem("Discord", nullptr, nullptr, ImGui::GetCurrentContext()->PlatformIO.Platform_OpenInShellFn)) {
                ImGui::GetCurrentContext()->PlatformIO.Platform_OpenInShellFn(ImGui::GetCurrentContext(), kLinkDiscord);
            }

            if (ImGui::MenuItem("Github", nullptr, nullptr, ImGui::GetCurrentContext()->PlatformIO.Platform_OpenInShellFn)) {
                ImGui::GetCurrentContext()->PlatformIO.Platform_OpenInShellFn(ImGui::GetCurrentContext(), kLinkGithub);
            }

            ImGui::EndMenu();
        }

        ImGui::EndMainMenuBar();
    }

    if (mViewDebugInfo) {
        if (ImGui::Begin("Debug", &mViewDebugInfo)) {
            std::string path = path_to_string(mThumperPath.value());
            ImGui::LabelText("Thumper Path", "%s", path.c_str());
        }
        ImGui::End();
    }

    hash_panel(mShowHashPanel);
    about_panel(mIconTexture, mShowAboutPanel);

    if (ImGui::Begin("Parsed Objlibs")) {
        for (auto& level : gLevels) {
             if (ImGui::TreeNode(level.origin.c_str())) {
                if (ImGui::TreeNode("Leafs")) {
                    ImGui::PushID(level.origin.c_str());

                    for (auto& leaf : level._leafs) {
                        if (ImGui::SmallButton(leaf._declaredName.c_str())) {
                            mContext.pLeaf = &leaf;
                            mContext.leafTitle = std::format("{}:{}###LEAFVIEWER", level.origin, leaf._declaredName);
                        }

                        if (ImGui::BeginPopupContextItem()) {
                            if (ImGui::Button("Jump to offset in objlib")) {
                                mContext.memoryEditorOffset = level._bytes.data();
                                mContext.memoryEditorSize = level._bytes.size();
                                mContext.memoryEditor.GotoAddrAndHighlight(leaf._beginOffset, leaf._endOffset);
                                ImGui::CloseCurrentPopup();
                            }

                            ImGui::EndPopup();
                        }

                        ImGui::SetItemTooltip("Offset from 0x%x to 0x%x (%d bytes)", leaf._beginOffset, leaf._endOffset, leaf._endOffset - leaf._beginOffset);
                    }

                    ImGui::PopID();

                    ImGui::TreePop();
                }

                if (ImGui::TreeNode("Samps")) {
                    ImGui::PushID(level.origin.c_str());

                    for (auto& samp : level._samps) {
                        if (ImGui::SmallButton(samp._declaredName.c_str())) {
                            mContext.pSample = &samp;
                            mContext.sampleTitle = std::format("{}:{}###SAMPVIEWER", level.origin, samp._declaredName);
                        }

                        if (ImGui::BeginPopupContextItem()) {
                            if (ImGui::Button("Jump to offset in objlib")) {
                                mContext.memoryEditorOffset = level._bytes.data();
                                mContext.memoryEditorSize = level._bytes.size();
                                mContext.memoryEditor.GotoAddrAndHighlight(samp._beginOffset, samp._endOffset);
                                ImGui::CloseCurrentPopup();
                            }

                            ImGui::EndPopup();
                        }

                        ImGui::SetItemTooltip("Offset from 0x%x to 0x%x (%d bytes)", samp._beginOffset, samp._endOffset, samp._endOffset - samp._beginOffset);
                    }

                    ImGui::PopID();

                    ImGui::TreePop();
                }

                if (ImGui::TreeNode("Masters")) {
                    ImGui::PushID(level.origin.c_str());

                    for (auto& master : level._masters) {
                        if (ImGui::SmallButton(master._declaredName.c_str())) {
                            mContext.pMaster = &master;
                            mContext.masterTitle = std::format("{}:{}###MASTERVIEWER", level.origin, master._declaredName);
                        }

                        if (ImGui::BeginPopupContextItem()) {
                            if (ImGui::Button("Jump to offset in objlib")) {
                                mContext.memoryEditorOffset = level._bytes.data();
                                mContext.memoryEditorSize = level._bytes.size();
                                mContext.memoryEditor.GotoAddrAndHighlight(master._beginOffset, master._endOffset);
                                ImGui::CloseCurrentPopup();
                            }

                            ImGui::EndPopup();
                        }

                        ImGui::SetItemTooltip("Offset from 0x%x to 0x%x (%d bytes)", master._beginOffset, master._endOffset, master._endOffset - master._beginOffset);
                    }

                    ImGui::PopID();

                    ImGui::TreePop();
                }

                if (ImGui::TreeNode("Drawers")) {
                    ImGui::PushID(level.origin.c_str());

                    for (auto& drawer : level._drawers) {
                        if (ImGui::SmallButton(drawer._declaredName.c_str())) {
                            mContext.pDrawer = &drawer;
                            mContext.drawerTitle = std::format("{}:{}###DRAWERVIEWER", level.origin, drawer._declaredName);
                        }

                        if (ImGui::BeginPopupContextItem()) {
                            if (ImGui::Button("Jump to offset in objlib")) {
                                mContext.memoryEditorOffset = level._bytes.data();
                                mContext.memoryEditorSize = level._bytes.size();
                                mContext.memoryEditor.GotoAddrAndHighlight(drawer._beginOffset, drawer._endOffset);
                                ImGui::CloseCurrentPopup();
                            }

                            ImGui::EndPopup();
                        }

                        ImGui::SetItemTooltip("Offset from 0x%x to 0x%x (%d bytes)", drawer._beginOffset, drawer._endOffset, drawer._endOffset - drawer._beginOffset);
                    }

                    ImGui::PopID();

                    ImGui::TreePop();
                }

                if (ImGui::TreeNode("Spns")) {
                    ImGui::PushID(level.origin.c_str());

                    for (auto& spn : level._spns) {
                        if (ImGui::SmallButton(spn._declaredName.c_str())) {
                            mContext.pSpn = &spn;
                            mContext.spnTitle = std::format("{}:{}###SPNVIEWER", level.origin, spn._declaredName);
                        }

                        if (ImGui::BeginPopupContextItem()) {
                            if (ImGui::Button("Jump to offset in objlib")) {
                                mContext.memoryEditorOffset = level._bytes.data();
                                mContext.memoryEditorSize = level._bytes.size();
                                mContext.memoryEditor.GotoAddrAndHighlight(spn._beginOffset, spn._endOffset);
                                ImGui::CloseCurrentPopup();
                            }

                            ImGui::EndPopup();
                        }

                        ImGui::SetItemTooltip("Offset from 0x%x to 0x%x (%d bytes)", spn._beginOffset, spn._endOffset, spn._endOffset - spn._beginOffset);
                    }

                    ImGui::PopID();

                    ImGui::TreePop();
                }

                if (ImGui::TreeNode("Library Imports")) {
                    for (auto & import : level.libraryImports) {
                        ImGui::TextUnformatted(import.library.c_str());
                    }
                    ImGui::TreePop();
                }
                
                if (ImGui::TreeNode("Object Imports")) {
                    for (auto & import : level.objectImports) {
                        ImGui::Text("%s @ %s", import.name.c_str(), import.library.c_str());
                    }
                    ImGui::TreePop();
                }

                if (ImGui::TreeNode("Object Declarations")) {
                    for (auto & import : level.objectDeclarations) {
                        ImGui::PushStyleColor(ImGuiCol_Text, import._definitionOffset == 0 ? ImVec4(1.0f, 0.0f, 0.0f, 1.0f) : ImVec4(0.0f, 1.0f, 0.0f, 1.0f));
                        ImGui::TextUnformatted(import.name.c_str());
                        ImGui::PopStyleColor();
                        ImGui::SetItemTooltip("0x%x", import.type);
                    }
                    ImGui::TreePop();
                }

                ImGui::TreePop();
            }
        }
    }
    ImGui::End();

    gui_memory_editor();    
    gui_sample_viewer();
    gui_leaf_viewer();
    gui_spn_viewer();
    gui_master_viewer();
    gui_drawer_viewer();

    aurora::gui_diff_table(mShowDifficultyExplanation, mDiffTextures);

#if 0
    if (ImGui::Begin("Thumper Level Editor v0.0.0.1", nullptr, ImGuiWindowFlags_MenuBar))
    {
        if (ImGui::BeginMenuBar())
        {
            if (ImGui::BeginMenu("File"))
            {
                if (ImGui::MenuItem("Save All", "CTRL+Q"))
                {
                    //Save all changes made to the objlib
                }

                if (ImGui::MenuItem("Open Level...", "CTRL+P"))
                {
                    //Open an objlib / level
                }

                if (ImGui::MenuItem("Recent Levels...", "CTRL+R"))
                {
                    //Open list of recently opened levels, will initially be none on first startup
                }

                ImGui::Separator();

                if (ImGui::MenuItem("Open current level in explorer", "CTRL+E"))
                {
                    //Open the currently opened level / objlib in file explorer
                }



                if (ImGui::BeginMenu("Leaf..."))
                {
                    if (ImGui::MenuItem("New", "CTRL+N"))
                    {
                        //Create a new, empty leaf file
                    }

                    if (ImGui::MenuItem("Open", "CTRL+O"))
                    {
                        //Open an existing leaf file
                    }

                    if (ImGui::MenuItem("Save", "CTRL+S"))
                    {
                        //Save the currently open leaf file
                    }

                    if (ImGui::MenuItem("Save As...", "CTRL+A"))
                    {
                        //Save the currently open leaf file as a different name / separate file
                    }

                    if (ImGui::MenuItem("Open Template", "CTRL+T"))
                    {
                        //Open a pre-made template of a leaf file
                    }

                    ImGui::EndMenu();
                }



                if (ImGui::BeginMenu("LVL..."))
                {
                    if (ImGui::MenuItem("New", "ALT+N"))
                    {
                        //Create a new, empty lvl file
                    }

                    if (ImGui::MenuItem("Open", "ALT+O"))
                    {
                        //Open an existing lvl file
                    }

                    if (ImGui::MenuItem("Save", "ALT+S"))
                    {
                        //Save the currently open lvl file
                    }

                    if (ImGui::MenuItem("Save As...", "ALT+A"))
                    {
                        //Save the currently open lvl file as a different name / separate file
                    }

                    ImGui::EndMenu();
                }



                if (ImGui::BeginMenu("Gate..."))
                {
                    if (ImGui::MenuItem("New", "CTRL+SHIFT+N"))
                    {
                        //Create a new, empty gate file
                    }

                    if (ImGui::MenuItem("Open", "CTRL+SHIFT+O"))
                    {
                        //Open an existing gate file
                    }

                    if (ImGui::MenuItem("Save", "CTRL+SHIFT+S"))
                    {
                        //Save the currently open gate file
                    }

                    if (ImGui::MenuItem("Save As...", "CTRL+SHIFT+A"))
                    {
                        //Save the currently open gate file as a different name / separate file
                    }

                    ImGui::EndMenu();
                }



                if (ImGui::BeginMenu("Master..."))
                {
                    if (ImGui::MenuItem("New", "CTRL+ALT+N"))
                    {
                        //Create a new, empty master sequence file
                    }

                    if (ImGui::MenuItem("Open", "CTRL+ALT+O"))
                    {
                        //Open an existing master sequence file
                    }

                    if (ImGui::MenuItem("Save", "CTRL+ALT+S"))
                    {
                        //Save the currently open master sequence file
                    }

                    if (ImGui::MenuItem("Save As...", "CTRL+ALT+A"))
                    {
                        //Save the currently open master sequence file as a different name / separate file
                    }

                    ImGui::EndMenu();
                }

                if (ImGui::BeginMenu("Sample..."))
                {
                    if (ImGui::MenuItem("New", "ALT+SHIFT+N"))
                    {
                        //Create a new, empty .samp container file
                    }

                    if (ImGui::MenuItem("Open", "ALT+SHIFT+O"))
                    {
                        //Open an existing lvl .samp container file
                    }

                    if (ImGui::MenuItem("Save", "ALT+SHIFT+S"))
                    {
                        //Save the currently open .samp container file
                    }

                    if (ImGui::MenuItem("Save As...", "ALT+SHIFT+A"))
                    {
                        //Save the currently open .samp container file as a different name / separate file
                    }

                    ImGui::EndMenu();
                }

                ImGui::Separator();

                if (ImGui::MenuItem("New Level Folder", "CTRL+L"))       // Fill background of button light green to highlight
                {
                    //Create a new project folder that contains all the files of the custom level.
                }

                if (ImGui::MenuItem("Edit Level Details"))       // Fill background of button light grey to highlight
                {
                    //Create a new details file that stores info about the level - description, authors, bpm etc...
                }

                if (ImGui::MenuItem("Regenerate Default Files"))       // Fill background of button light grey to highlight.        
                {
                    //No comment, I actually don't know what this does
                }

                if (ImGui::BeginMenu("Template files..."))
                {
                    if (ImGui::MenuItem("Open folder"))
                    {
                        //Open the folder that contains leaf template files
                    }

                    if (ImGui::MenuItem("Regenerate files"))
                    {
                        //Probably regenerates the default template files if they got deleted by accident
                    }

                    ImGui::EndMenu();
                }

                ImGui::Separator();

                if (ImGui::MenuItem("Exit"))
                {
                    //Probably not needed anymore, but originally closes the entire editor program. Could probably re-use this to close the currently open level.
                }

                ImGui::EndMenu();
            }

            if (ImGui::BeginMenu("Edit"))
            {

                if (ImGui::MenuItem("Preferences..."))
                {
                    /*

                    This opens a separate window that allows you to:
                    - change the default colors of the editor
                        - menu color
                        - master sequence color
                        - lvl editor color
                        - sample editor color
                        - gate editor color
                        - leaf editor color
                        - active panel color
                        - background color
                        - default track object selected (would probably be removed later) and its param
                    - audio
                        - mute application audio
                    - keybinds
                        - every keybind. leaf new, leaf open, template.. everything. Includes a search function.

                    */
                }

                ImGui::EndMenu();
            }

            if (ImGui::BeginMenu("View"))
            {
                ImGui::MenuItem("Leaf Editor", nullptr, &mLeafEditorShown);
                ImGui::MenuItem("Lvl Editor", nullptr, &mLvlEditorShown);
                ImGui::MenuItem("Gate Editor", nullptr, &mGateEditorShown);
                ImGui::MenuItem("Master Editor", nullptr, &mMasterEditorShown);
                ImGui::MenuItem("Working Folder", nullptr, &mWorkingFolderShown);
                ImGui::MenuItem("Sample Editor", nullptr, &mSampleEditorShown);

                ImGui::EndMenu();
            }

            if (ImGui::BeginMenu("Window"))
            {
                //No clue what this is.

                ImGui::EndMenu();
            }

            if (ImGui::BeginMenu("Help"))
            {
                //Fill this in later because I cannot be bothered

                ImGui::EndMenu();
            }

            ImGui::EndMenuBar();
        }
    }
    ImGui::End();
#endif

    if (ImGui::Begin("Thumper Mod Loader")) {
        ImGui::TextUnformatted("Mod Mode");
        ImGui::SameLine();

        if (mModMode) {
            ImGui::PushStyleColor(ImGuiCol_Button, { 154.0f / 255.0f, 205.0f / 255.0f , 50.0f / 255.0f, 1.0f });
            if (ImGui::SmallButton("is ON")) mModMode ^= true;
        }
        else {
            ImGui::PushStyleColor(ImGuiCol_Button, { 64.0f / 255.0f,0,0,1 });
            if (ImGui::SmallButton("is OFF")) mModMode ^= true;
        }
        ImGui::PopStyleColor();


        ImGui::BeginDisabled(!mModMode);
        ImGui::Button("Update Levels");
        ImGui::EndDisabled();
        ImGui::SetItemTooltip("%s", "Update Thumper with these levels and splash screen.\nAdding or removing levels requires a re-launch of the game.");

        ImGui::SeparatorText("Levels");

        if (ImGui::BeginTable("ModeLoaderLevlTable", 4, ImGuiTableFlags_BordersInner)) {

            ImGui::TableNextRow();
            ImGui::TableNextColumn();
            ImGui::TextUnformatted("Level Name");

            ImGui::TableNextColumn();
            ImGui::TextUnformatted("Difficulty");
            ImGui::SameLine();
            if (ImGui::SmallButton("?"))
                mShowDifficultyExplanation = true;

            ImGui::TableNextColumn();
            ImGui::TextUnformatted("Description");

            ImGui::TableNextColumn();
            ImGui::TextUnformatted("Author");

            for (auto const& level : mLevels) {
                ImGui::TableNextRow();

                ImGui::TableNextColumn();
                ImGui::TextUnformatted(level.name.c_str());

                ImGui::TableNextColumn();
                ImGui::TextUnformatted(level.difficulty.c_str());

                ImGui::TableNextColumn();
                ImGui::TextUnformatted(level.description.c_str());

                ImGui::TableNextColumn();
                ImGui::TextUnformatted(level.author.c_str());
            }

            ImGui::EndTable();
        }
    }
    ImGui::End();

    if (mShowDearImGuiDemo) ImGui::ShowDemoWindow(&mShowDearImGuiDemo);
}

int main(int argc, char** argv) {
    Application app;
    app.run();
    return EXIT_SUCCESS;
}