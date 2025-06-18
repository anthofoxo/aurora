-- Gives a visual presentation of .pc files

local files = {}

for _, entry in ipairs(Aurora.filesystem.directory_iterator(Aurora.game_directory() .. "/cache/") or {}) do
    entry = Aurora.filesystem.stem(entry)
    local key = tonumber(entry, 16)
    if key then files[key] = entry end
end

for _, filename in ipairs(dofile("hashtable/pc_list.lua")) do
    if Aurora.filesystem.exists(string.format("%s/cache/%x.pc", Aurora.game_directory(), Aurora.hash(filename))) then
        files[Aurora.hash(filename)] = filename:sub(2)
    end
end

local function mysplit(inputstr, sep)
  if sep == nil then sep = "%s" end
  local t = {}
  for str in string.gmatch(inputstr, "([^"..sep.."]+)") do table.insert(t, str) end
    return t
end

-- iterable table
local structured = {}

for key, value in pairs(files) do
    local tokens = mysplit(value, "/")
    local count = #tokens
    local ref = structured

    for i = 1, count do
        local token = tokens[i]

        if i == count then
            local type = "unknown"

            if string.match(token, "%.x") or string.match(token, "%.X") then
                type = "mesh"
            end

            ref[token] = {
                terminate = true,
                hash = key,
                type = type,
            }
        else
            if not ref[token] then ref[token] = {} end
            ref = ref[token]
        end
    end
end

local function recurse(vals)
     for key, value in pairs(vals) do
        if value.terminate then
            local opened = ImGui.TreeNodeEx(Aurora.escape(key), 256)

            local actionOpenMesh = function()
                Aurora.SendMessage("aurora.mesh_viewer", "open", {
                    file = string.format("%s/cache/%x.pc", Aurora.game_directory(), value.hash)
                })
            end

            if ImGui.IsItemActivated() then
                if value.type == "mesh" then actionOpenMesh() end
            end

            ImGui.SetItemTooltip(string.format("File type: %s", value.type))

            if ImGui.BeginPopupContextItem() then
                if ImGui.MenuItem("Copy Hash") then
                    ImGui.LogToClipboard()
                    ImGui.LogText("%x", value.hash)
                    ImGui.LogFinish()
                    ImGui.CloseCurrentPopup()
                end

                ImGui.Separator()

                if ImGui.MenuItem("Open in mesh viewer") then
                    actionOpenMesh()
                    ImGui.CloseCurrentPopup()
                end

                ImGui.EndPopup()
            end

            if opened then ImGui.TreePop() end
        else
            if ImGui.TreeNode(key) then
                recurse(value)
                ImGui.TreePop()
            end
        end
    end
end

return {
	gui = {
		title = "File Structure",
        OnGui = function()
		    recurse(structured)
		end
	},
}