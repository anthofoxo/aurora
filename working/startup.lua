print "This is an aurora script, these are written in lua"
print "All lua standard libraries are available"
print "Use `thumper_hash(string)` to hash something!"

local input = "Alevels/demo.objlib"
local output = thumper_hash(input)

print(string.format("hash(%q) = 0x%x", input, output))

print "See startup.lua for more :D"