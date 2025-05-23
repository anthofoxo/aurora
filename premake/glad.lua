project "glad"

location "%{wks.location}/vendor/%{prj.name}"
language "C"

files "%{prj.location}/src/gl.c"
includedirs "%{prj.location}/include"