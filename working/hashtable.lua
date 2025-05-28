local levels = {
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
}


local hashtable = {}

for _, value in ipairs(levels) do
	hashtable[aurora_hash(value)] = value
end

return hashtable