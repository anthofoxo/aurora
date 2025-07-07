project "aurora"
kind "SharedLib"

debugcommand "C:/Program Files (x86)/Steam/steamapps/common/Thumper/THUMPER_win8.exe.unpacked.exe"
debugdir "C:/Program Files (x86)/Steam/steamapps/common/Thumper"

postbuildcommands { "powershell -NoProfile -ExecutionPolicy Bypass -File \"post_build.ps1\"" }

files {
	"**.c",
	"**.cpp",
	"**.h",
	"**.hpp",
	"**.inl",
}

includedirs {
	"%{prj.location}",
	"%{wks.location}/vendor/glfw/include",
	"%{wks.location}/vendor/glad/include",
	"%{wks.location}/vendor/imgui",
	"%{wks.location}/vendor/lua/src",
	"%{wks.location}/vendor/tinyfd",
	"%{wks.location}/vendor/spdlog/include",
	"%{wks.location}/vendor/glm",
	"%{wks.location}/vendor/steamworks",
	"%{wks.location}/vendor/zlib",
	"%{wks.location}/vendor/zlib/contrib",
}

links {
	"glfw",
	"glad",
	"imgui",
	"lua",
	"tinyfd",
	"zlib"
}

filter "system:windows"
links "opengl32"
defines "FMT_UNICODE=0"
