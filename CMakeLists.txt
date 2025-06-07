cmake_minimum_required(VERSION 3.10)
set(CMAKE_CXX_STANDARD 23)

add_subdirectory(vendor/glfw)
add_subdirectory(vendor/glad)
add_subdirectory(vendor/spdlog)
add_subdirectory(vendor)

project(aurora)

add_executable(aurora
        aurora/au_entrypoint.cpp
        aurora/au_aurora.cpp
        aurora/imgui_memory_editor.h
        aurora/imspinner.h
        aurora/lua_api.cpp
        aurora/lua_api.hpp
        aurora/sha1.hpp
        aurora/au_util.hpp
        aurora/au_util.cpp)

target_link_libraries(aurora glfw glad imgui tinyfd lua spdlog)