local hashes = {
	----------------------
	-- Translation Keys --
	----------------------
	"accept",
	"no",
	"level1",
	"level2",
	"level3",
	"level4",
	"level5",
	"level6",
	"level7",
	"level8",
	"level9",
	"cancel",
	"continue",
	"play",
	"yes",
	"leaderboard_view",
	"rank",
	"retry",
	"tip",
	-----------
	-- Other --
	-----------
	"Aachievement/config.ach",
	"Achannels.objlib",
	"Edefaults.objlib",
	"Edrool_splash.objlib",
	------------
	-- Global --
	------------
	"Aglobal/chrome_aurora_tex_256.objlib",
	"Aglobal/chrome_tex_128.objlib",
	"Aglobal/chrome_tex_256.objlib",
	"Aglobal/gongs.objlib",
	"Aui/error_dialog.objlib",
	------------
	-- Entity --
	------------
	"Aentity/ambient_fx.objlib",
	--------
	-- UI --
	--------
	"Aui/hmd_help.objlib",
	"Aui/rank_icons.objlib",
	"Aui/strings.en.loc",
	"Aui/thumper.levels",
	"Aui/thumper.scoring",
	------------
	-- Events --
	------------
	"Aevent/app.event",
	"Eevent/core.event",
	"Eevent/ui.event",
	-------------
	-- Samples --
	-------------
	"Asamples/levels/global_drones/french_horn_notes/pattern17.wav",
	------------
	-- Levels --
	------------
	"Alevels/title_screen.objlib",
	"Alevels/demo.objlib",
	"Alevels/level2/level_2a.objlib",
	"Alevels/level3/level_3a.objlib",
	"Alevels/level4/level_4a.objlib",
	"Alevels/level5/level_5a.objlib",
	"Alevels/level6/level_6.objlib",
	"Alevels/level7/level_7a.objlib",
	"Alevels/level8/level_8a.objlib",
	"Alevels/level9/level_9a.objlib",
	--------------------
	-- Level sections --
	--------------------
	"Alevels/demo.sec",
	"Alevels/level2/level_2a.sec",
	"Alevels/level3/level_3a.sec",
	"Alevels/level4/level_4a.sec",
	"Alevels/level5/level_5a.sec",
	"Alevels/level6/level_6.sec",
	"Alevels/level7/level_7a.sec",
	"Alevels/level8/level_8a.sec",
	"Alevels/level9/level_9a.sec",
}

for _, value in ipairs(dofile("hashtable/textures.lua")) do
	table.insert(hashes, value)
end

for _, value in ipairs(dofile("hashtable/meshes.lua")) do
	table.insert(hashes, value)
end

local hashtable = {}

for _, value in ipairs(hashes) do
	hashtable[aurora_hash(value)] = value
end

return hashtable
