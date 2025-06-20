#include <exception>
#include <cstdlib>
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

#ifdef _WIN32
int WINAPI WinMain(_In_ HINSTANCE hInstance, _In_opt_ HINSTANCE hPrevInstance, _In_ LPSTR lpCmdLine, _In_ int nShowCmd)
#else
int main(int, char**)
#endif
{
	aurora::main();
	return EXIT_SUCCESS;
}