workspace "aurora"
architecture "x86_64"
configurations { "debug", "release" }

flags "MultiProcessorCompile"
language "C++"
cppdialect "C++23"
cdialect "C17"
staticruntime "On"
stringpooling "On"
editandcontinue "On"

kind "StaticLib"
targetdir "%{wks.location}/bin/%{cfg.system}_%{cfg.buildcfg}"
objdir "%{wks.location}/bin_int/%{cfg.system}_%{cfg.buildcfg}"

filter "configurations:debug"
runtime "Debug"
optimize "Debug"
symbols "On"
defines "_DEBUG"

filter "configurations:release"
runtime "Release"
optimize "Speed"
symbols "Off"
defines "NDEBUG"

filter "system:windows"
systemversion "latest"
defines { "NOMIXMAX", "WIN32_LEAN_AND_MEAN" }
buildoptions { "/EHsc", "/Zc:throwingNew", "/Zc:preprocessor", "/Zc:__cplusplus", "/experimental:c11atomics" }

startproject "aurora"
include "aurora/build.lua"

group "dependencies"
for _, matchedfile in ipairs(os.matchfiles("premake/*.lua")) do
	include(matchedfile)
end
