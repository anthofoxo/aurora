//====== Copyright Valve Corporation, All rights reserved. ====================
//
// This header includes *all* of the interfaces and callback structures
// in the Steamworks SDK, and some high level functions to control the SDK
// (init, shutdown, etc) that you probably only need in one or two files.
//
// To save your compile times, we recommend that you not include this file
// in header files.  Instead, include the specific headers for the interfaces
// and callback structures you need.  The one file you might consider including
// in your precompiled header (e.g. stdafx.h) is steam_api_common.h
//
//====== Edits made by anthofoxo ==============================================
//
// This version is slightly different than the standard `steam_api.h` header
// to avoid conficting with hooking the dll.
//
// 1. The Win32 check for `#pragma once` is removed
// 2. `STEAM_API_EXPORTS` is *always* defined
// 3. The Steam API functions are *not* declared or defined
//
//=============================================================================

#ifndef STEAM_API_H
#define STEAM_API_H

#define STEAM_API_EXPORTS

#include "steam/isteamapplist.h"
#include "steam/isteamapps.h"
#include "steam/isteamclient.h"
#include "steam/isteamcontroller.h"
#include "steam/isteamfriends.h"
#include "steam/isteamhtmlsurface.h"
#include "steam/isteamhttp.h"
#include "steam/isteaminput.h"
#include "steam/isteaminventory.h"
#include "steam/isteammatchmaking.h"
#include "steam/isteammusic.h"
#include "steam/isteammusicremote.h"
#include "steam/isteamnetworking.h"
#include "steam/isteamnetworkingmessages.h"
#include "steam/isteamnetworkingsockets.h"
#include "steam/isteamnetworkingutils.h"
#include "steam/isteamparentalsettings.h"
#include "steam/isteamremoteplay.h"
#include "steam/isteamremotestorage.h"
#include "steam/isteamscreenshots.h"
#include "steam/isteamugc.h"
#include "steam/isteamuser.h"
#include "steam/isteamuserstats.h"
#include "steam/isteamutils.h"
#include "steam/isteamvideo.h"
#include "steam/steam_api_common.h"

#endif