project "tinyfd"

location "%{wks.location}/vendor/%{prj.name}"
language "C"

files {
	"%{prj.location}/tinyfiledialogs.c",
	"%{prj.location}/tinyfiledialogs.h"
}

filter "system:windows"
defines "_CRT_SECURE_NO_WARNINGS"