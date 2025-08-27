#include "au_hooks.hpp"

#include <Windows.h>

namespace {
HMODULE gSteamModule = nullptr;
}

#define STEAM_FORWARD(name, ...) reinterpret_cast<decltype(&name)>(::GetProcAddress(gSteamModule, #name))(__VA_ARGS__)

#include "au_steam_api.h"

#include <cstdio>
#include <cstdlib>
#include <iostream>

S_API bool S_CALLTYPE SteamAPI_RestartAppIfNecessary(uint32 unOwnAppID) {
	gSteamModule = ::LoadLibraryA("steam_api64.dll.bak");
	if (STEAM_FORWARD(SteamAPI_RestartAppIfNecessary, unOwnAppID)) return true;

	// Makes sure console logs go to the right process
	// if (AttachConsole(ATTACH_PARENT_PROCESS)) {
	if (AllocConsole()) {
		FILE* fp;
		freopen_s(&fp, "CONOUT$", "w", stdout);
		freopen_s(&fp, "CONOUT$", "w", stderr);
		std::ios::sync_with_stdio();
		std::cout.clear();
		std::cerr.clear();
	}

	aurora::main();
	if (!aurora::should_launch_thumper()) {
		std::exit(0);
	}

	return false;
}

S_API bool S_CALLTYPE SteamAPI_Init() { return STEAM_FORWARD(SteamAPI_Init); }
S_API void S_CALLTYPE SteamAPI_Shutdown() { STEAM_FORWARD(SteamAPI_Shutdown); }
S_API void* S_CALLTYPE SteamInternal_FindOrCreateUserInterface(HSteamUser hSteamUser, const char* pszVersion) {
	return STEAM_FORWARD(SteamInternal_FindOrCreateUserInterface, hSteamUser, pszVersion);
}
S_API void* S_CALLTYPE SteamInternal_ContextInit(void* pContextInitData) { return STEAM_FORWARD(SteamInternal_ContextInit, pContextInitData); }
S_API HSteamUser S_CALLTYPE SteamAPI_GetHSteamUser() { return STEAM_FORWARD(SteamAPI_GetHSteamUser); }
S_API void S_CALLTYPE SteamAPI_UnregisterCallResult(class CCallbackBase* pCallback, SteamAPICall_t hAPICall) {
	return STEAM_FORWARD(SteamAPI_UnregisterCallResult, pCallback, hAPICall);
}
S_API void SteamAPI_RegisterCallResult(class CCallbackBase* pCallback, SteamAPICall_t hAPICall) { return STEAM_FORWARD(SteamAPI_RegisterCallResult, pCallback, hAPICall); }
S_API void S_CALLTYPE SteamAPI_RunCallbacks() { return STEAM_FORWARD(SteamAPI_RunCallbacks); }