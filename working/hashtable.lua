local hashes = {
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
}

for _, value in ipairs(dofile("hashtable/pc_list.lua")) do table.insert(hashes, value) end

if Aurora.filesystem.exists("hashtable/userdef.lua") then
	for _, value in ipairs(dofile("hashtable/userdef.lua")) do table.insert(hashes, value) end
end

local hashtable = {}

for _, value in ipairs(hashes) do
	hashtable[Aurora.hash(value)] = value
end

return hashtable
