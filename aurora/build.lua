project "aurora"
kind "WindowedApp"
debugdir "../working"

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
}

links {
	"glfw",
	"glad",
	"imgui",
	"lua",
	"tinyfd",
}

filter "system:windows"
links "opengl32"
defines "FMT_UNICODE=0"
