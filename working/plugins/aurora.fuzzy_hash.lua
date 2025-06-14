local input = Aurora.box("")
local output = {}

return {
	gui = {
		title = "Hasher",
		OnGui = function()
			if ImGui.InputText("Input", input) then
				local unboxed = Aurora.unbox(input)
                output = {}

				local hashtable = Aurora.hashtable()

                for key, value in pairs(hashtable) do

                    value = Aurora.escape(value)

                    local idx0, idx1 = string.find(value, unboxed)

                    if idx0 and idx1 then
                        local match = {}
                        match.prefix = Aurora.escape(string.sub(value, 1, idx0 - 1))
                        match.group = Aurora.escape(string.sub(value, idx0, idx1))
                        match.suffix = Aurora.escape(string.sub(value, idx1 + 1, -1))
                        table.insert(output, match)
                    end
                end
			end

            for _, result in ipairs(output) do
                ImGui.PushStyleVar(14, { 0, 0 })
                ImGui.TextUnformatted(result.prefix)
                ImGui.SameLine()
                ImGui.TextColored({ 0.0, 1.0, 0.0, 1.0 }, "%s", result.group)
                ImGui.SameLine()
                ImGui.TextUnformatted(result.suffix)
                ImGui.PopStyleVar()
            end
		end
	},
}