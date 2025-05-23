project "imgui"

location "%{wks.location}/vendor/%{prj.name}"

includedirs {
	"%{prj.location}",
	"%{wks.location}/vendor/glfw/include",
	"%{wks.location}/vendor/glad/include",
}

files {
	"%{prj.location}/*.cpp",
	"%{prj.location}/*.h",
	"%{prj.location}/backends/imgui_impl_opengl3.cpp",
	"%{prj.location}/backends/imgui_impl_opengl3.h",
	"%{prj.location}/backends/imgui_impl_glfw.cpp",
	"%{prj.location}/backends/imgui_impl_glfw.h",
	"%{prj.location}/misc/cpp/imgui_stdlib.cpp",
	"%{prj.location}/misc/cpp/imgui_stdlib.h",
}