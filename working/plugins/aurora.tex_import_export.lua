local input = Aurora.box("")

local texture = nil
local selection = nil
local exportPerformed = nil

local knownTextures = {}

Aurora.filesystem.create_directory("./tmp")

for _, value in pairs(Aurora.hashtable()) do
    if string.match(value, ".png") then
        table.insert(knownTextures, value)
    end
end

---@param filename string
---@return string?
local function perform_export(filename)
    local filepath = string.format("%s/cache/%x.pc", Aurora.game_directory(), Aurora.hash(filename))
    local filebytes = Aurora.read_file(filepath)

    if not filebytes then
        return nil
    end

    filebytes = string.sub(filebytes, 5)

    local substitutedString = "./tmp/" .. string.gsub(string.gsub(Aurora.escape(filename), "\\", "_"), "/", "_");

    -- Cap path length at 64
    if #substitutedString > 64 then
        substitutedString = string.sub(substitutedString, 1, math.min(64, #substitutedString));
    end

    substitutedString = substitutedString .. ".dds"

    local success = Aurora.write_file(substitutedString, filebytes);

    if success then
        return substitutedString
    else
        return nil
    end
end

return {
    OnUnload = function()
        if texture then
            gl.DeleteTextures(texture)
        end
    end,
	gui = {
		title = "Hasher",
        OnGui = function()
            ImGui.Text("%d known textures", #knownTextures)
            ImGui.Separator()

            ImGui.Columns(2);

            if ImGui.BeginChild("TextureExportScrollRegion") then
                for _, value in ipairs(knownTextures) do
                    if ImGui.Selectable(Aurora.escape(value), selection == value) then
                        selection = value
                        exportPerformed = nil

                        local filepath = string.format("%s/cache/%x.pc", Aurora.game_directory(), Aurora.hash(value))
                        local filebytes = Aurora.read_file(filepath)
                        if filebytes then
                            filebytes = string.sub(filebytes, 5)

                            if texture then
                                gl.DeleteTextures(texture)
                            end

                            texture = Aurora.ddsktx_parse(filebytes)
                        end
                    end
                end
            end
            ImGui.EndChild()

            ImGui.NextColumn();

        	if texture then
                ImGui.Image(texture, { 256, 256 })

                if ImGui.Button("Export DDS") then
                    local success = perform_export(selection)

                    if success then
                        exportPerformed = "Exported to " .. success
                    else
                        exportPerformed = "export failed"
                    end
                end

                if exportPerformed then
                    ImGui.TextUnformatted(exportPerformed);
                end
            end

		end
	},
}