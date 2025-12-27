#include "aurora_steam.h"

#include <Windows.h>
#include <strsafe.h>
#include <jni.h>

#include <string>
#include <iostream>
#include <optional>
#include <vector>
#include <filesystem>

#define STEAM_FORWARD(name, ...) reinterpret_cast<decltype(&name)>(GetProcAddress(gSteam, #name))(__VA_ARGS__)

static HMODULE gSteam = nullptr;

// Check registry for certain keys to point to the java installation
static TCHAR* tryFindJavaHome() {
	DWORD retval;
	HKEY jKey;

	if (retval = RegOpenKeyEx(HKEY_LOCAL_MACHINE, TEXT("SOFTWARE\\JavaSoft\\JDK"), 0, KEY_READ, &jKey)) {
		RegCloseKey(jKey);
		return nullptr;
	}

	TCHAR versionString[16];
	DWORD bufsize = 16 * sizeof(TCHAR);

	if (retval = RegGetValue(jKey, NULL, TEXT("CurrentVersion"), RRF_RT_REG_SZ, NULL, versionString, &bufsize)) {
		RegCloseKey(jKey);
		return nullptr;
	}

	TCHAR* dllpath = new TCHAR[512];
	bufsize = 512 * sizeof(TCHAR);
	retval = RegGetValue(jKey, versionString, TEXT("JavaHome"), RRF_RT_REG_SZ, NULL, dllpath, &bufsize);
	RegCloseKey(jKey);

	if (retval) {
		delete[] dllpath;
		return nullptr;
	}

	return dllpath;
}

static std::string getJavaExceptionStackTrace(JNIEnv* env) {
	if (!env->ExceptionCheck()) return "";

	jthrowable exc = env->ExceptionOccurred();
	if (!exc) return "Unknown exception";

	env->ExceptionClear();

	jclass stringWriterClass = env->FindClass("java/io/StringWriter");
	jclass printWriterClass = env->FindClass("java/io/PrintWriter");

	jmethodID swConstructor = env->GetMethodID(stringWriterClass, "<init>", "()V");
	jmethodID pwConstructor = env->GetMethodID(printWriterClass, "<init>", "(Ljava/io/Writer;)V");

	jobject swObj = env->NewObject(stringWriterClass, swConstructor);
	jobject pwObj = env->NewObject(printWriterClass, pwConstructor, swObj);

	jclass throwableClass = env->GetObjectClass(exc);
	jmethodID printStackTraceMethod = env->GetMethodID(throwableClass, "printStackTrace", "(Ljava/io/PrintWriter;)V");

	env->CallVoidMethod(exc, printStackTraceMethod, pwObj);

	jmethodID toStringMethod = env->GetMethodID(stringWriterClass, "toString", "()Ljava/lang/String;");
	jstring jstr = (jstring)env->CallObjectMethod(swObj, toStringMethod);

	const char* utfChars = env->GetStringUTFChars(jstr, nullptr);
	std::string result(utfChars);
	env->ReleaseStringUTFChars(jstr, utfChars);

	return result;
}

// If a Java exception excapes the vm, check for it and display an error message
static void checkJavaException(JNIEnv* env) {
	auto string = getJavaExceptionStackTrace(env);
	if (string.empty()) return;
	MessageBoxA(nullptr, string.c_str(), "Java Exception", MB_OK | MB_ICONERROR);
}

static bool gWantThumperLaunch = false;

static std::optional<std::string> findJar() {
	for (auto const& entry : std::filesystem::directory_iterator(".")) {
		if (entry.path().extension() == ".jar") { return entry.path().generic_string(); }
	}
	return std::nullopt;
}

static void spawnAurora() {
	TCHAR* javahome = tryFindJavaHome();
	if (javahome == nullptr) {
		std::cerr << "Cannot find java home via registry keys. Is your Java installed correctly?\n";
		exit(EXIT_FAILURE);
	}

	TCHAR dllPath[MAX_PATH];
	HRESULT hr = StringCchPrintf(dllPath, MAX_PATH, TEXT("%s\\bin\\server\\jvm.dll"), javahome);
	delete[] javahome;

	if (FAILED(hr)) {
		exit(EXIT_FAILURE);
	}

	HMODULE jniModule = LoadLibrary(dllPath);
	auto impl_JNI_GetDefaultJavaVMInitArgs = (decltype(&JNI_GetDefaultJavaVMInitArgs))GetProcAddress(jniModule, "JNI_GetDefaultJavaVMInitArgs");
	auto impl_JNI_CreateJavaVM = (decltype(&JNI_CreateJavaVM))GetProcAddress(jniModule, "JNI_CreateJavaVM");

	JavaVMInitArgs initArgs{};
	initArgs.version = JNI_VERSION_24;
	impl_JNI_GetDefaultJavaVMInitArgs(&initArgs);

	std::vector<JavaVMOption> options;
	std::string classpath = "-Djava.class.path=";

	auto jar = findJar();
	if (jar.has_value()) {
		std::cout << "Found .jar, running in user mode\n";

		classpath += *jar;
	}
	else {
		std::cout << "No jar found, is the aurora .jar next to THUMPER_win8.exe?\nAttempting to run in developer mode\n";

		if (std::filesystem::exists("aurora_lib")) {
			for (auto const& entry : std::filesystem::directory_iterator("aurora_lib")) {
				classpath += std::filesystem::absolute(entry.path()).generic_string();
				classpath += ";";
			}
		}

		classpath += std::filesystem::absolute("aurora_bin").generic_string();
		classpath += ";";
		classpath += std::filesystem::absolute("aurora_res").generic_string();
	}

	std::cout << classpath << '\n';

	options.emplace_back((char*)"-XX:+ShowCodeDetailsInExceptionMessages", nullptr);
	options.emplace_back((char*)"-Xcheck:jni", nullptr);
	options.emplace_back((char*)"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005", nullptr);
	options.emplace_back((char*)"--enable-native-access=ALL-UNNAMED", nullptr);
	options.emplace_back((char*)"--sun-misc-unsafe-memory-access=allow", nullptr);
	options.emplace_back((char*)"-Dfile.encoding=UTF-8", nullptr);
	options.emplace_back((char*)"-Dstdout.encoding=UTF-8", nullptr);
	options.emplace_back((char*)"-Dstderr.encoding=UTF-8", nullptr);
	options.emplace_back(classpath.data(), nullptr);

	initArgs.options = options.data();
	initArgs.nOptions = options.size();
	initArgs.ignoreUnrecognized = JNI_FALSE;

	JavaVM* jvm;
	JNIEnv* env;
	jint retvalue = impl_JNI_CreateJavaVM(&jvm, (void**)&env, &initArgs);

	jclass clClass = env->FindClass("java/lang/ClassLoader");
	jmethodID getSystemCL = env->GetStaticMethodID(clClass, "getSystemClassLoader", "()Ljava/lang/ClassLoader;");
	jobject systemCl = env->CallStaticObjectMethod(clClass, getSystemCL);
	jclass classLoaderClass = env->GetObjectClass(systemCl);
	jmethodID loadClass = env->GetMethodID(classLoaderClass, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
	jstring name = env->NewStringUTF("xyz.anthofoxo.aurora.EntryPoint");
	jclass clazz = (jclass)env->CallObjectMethod(systemCl, loadClass, name);
	checkJavaException(env);

	jmethodID mid = env->GetStaticMethodID(clazz, "auroraMain", "(Z)Z");
	checkJavaException(env);

	// 1 indicates that this is running in standalone mode
	gWantThumperLaunch = env->CallStaticBooleanMethod(clazz, mid, jboolean(1));
	checkJavaException(env);

	env->DeleteLocalRef(clazz);
	checkJavaException(env);

	jvm->DestroyJavaVM();
	FreeLibrary(jniModule);
}

S_API bool S_CALLTYPE SteamAPI_RestartAppIfNecessary(uint32 unOwnAppID) {
	if (AllocConsole()) {
		FILE* fp;
		freopen_s(&fp, "CONOUT$", "w", stdout);
		freopen_s(&fp, "CONOUT$", "w", stderr);
		std::ios::sync_with_stdio();
		std::cout.clear();
		std::cerr.clear();
	}

	gSteam = LoadLibraryA("steam_api64.dll.bak");

	if (gSteam == nullptr) {
		std::cerr << "Failed to load steam_api64.dll.bak\n";
		exit(EXIT_FAILURE);
	}

	if (STEAM_FORWARD(SteamAPI_RestartAppIfNecessary, unOwnAppID)) return true;

	spawnAurora();

	if (!gWantThumperLaunch) {
		exit(EXIT_SUCCESS);
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