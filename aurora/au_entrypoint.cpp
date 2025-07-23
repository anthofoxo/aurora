#include <cstdlib>
#include <exception>
#include <iostream>

#include <tinyfiledialogs.h>

#ifdef _WIN32
#	include <Windows.h>
#	undef max
#	undef min
#endif

namespace aurora {
void main();
}