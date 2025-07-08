project "zlib"

location "%{wks.location}/vendor/%{prj.name}"

includedirs {
	"%{prj.location}",
	"%{prj.location}/contrib/minizip",
}

files {
	"%{prj.location}/*.c",
	"%{prj.location}/*.h",
	"%{prj.location}/contrib/minizip/*.c",
	"%{prj.location}/contrib/minizip/*.h",
}

removefiles {
	"%{prj.location}/contrib/minizip/miniunz.c",
}