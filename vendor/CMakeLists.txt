cmake_minimum_required(VERSION 3.10)

project(imgui)
add_library(imgui
        imgui/imgui.cpp
        imgui/imgui_demo.cpp
        imgui/imgui_draw.cpp
        imgui/imgui_tables.cpp
        imgui/imgui_widgets.cpp
        imgui/misc/cpp/imgui_stdlib.cpp
        imgui/backends/imgui_impl_opengl3.cpp
        imgui/backends/imgui_impl_glfw.cpp)
target_include_directories(imgui PUBLIC imgui)
target_link_libraries(imgui glfw)

project(tinyfd)
add_library(tinyfd
        tinyfd/tinyfiledialogs.c
        tinyfd/tinyfiledialogs.h)
target_include_directories(tinyfd PUBLIC tinyfd)

project(lua)
add_library(lua
        lua/src/lapi.c
        lua/src/lauxlib.c
        lua/src/lbaselib.c
        lua/src/lcode.c
        lua/src/lcode.h
        lua/src/lcorolib.c
        lua/src/lctype.c
        lua/src/lctype.h
        lua/src/ldblib.c
        lua/src/ldebug.c
        lua/src/ldo.c
        lua/src/ldump.c
        lua/src/lfunc.c
        lua/src/lgc.c
        lua/src/linit.c
        lua/src/liolib.c
        lua/src/ljumptab.h
        lua/src/llex.c
        lua/src/llex.h
        lua/src/lmathlib.c
        lua/src/lmem.c
        lua/src/loadlib.c
        lua/src/lobject.c
        lua/src/lopcodes.c
        lua/src/lopcodes.h
        lua/src/lopnames.h
        lua/src/loslib.c
        lua/src/lparser.c
        lua/src/lparser.h
        lua/src/lstate.c
        lua/src/lstring.c
        lua/src/lstrlib.c
        lua/src/ltable.c
        lua/src/ltablib.c
        lua/src/ltm.c
        lua/src/lundump.c
        lua/src/lutf8lib.c
        lua/src/lvm.c
        lua/src/lzio.c)
target_include_directories(lua PUBLIC lua/src)