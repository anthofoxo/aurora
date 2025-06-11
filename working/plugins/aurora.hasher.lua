local input = Aurora.box("")
local output = 0
local cacheHit = false
local rhashHit = nil
local directLookup = nil

return {
	gui = {
		visible = true,
		title = "Hasher",
		OnGui = function()
            if ImGui.InputText("Input", input) then
                local unboxed = Aurora.unbox(input)

                local unescaped = Aurora.unescape(unboxed)
                output = Aurora.hash(unescaped)
                cacheHit = Aurora.cache_hit(string.format("%x.pc", output))
                rhashHit = Aurora.rhash(output)

                if rhashHit then
                    rhashHit = Aurora.escape(rhashHit)
                end

                directLookup = nil

                -- inputs larger than 8 chars will never have a direct hit
                if #unboxed <= 8 then
                    local result = tonumber(unboxed, 16)

                    if result then
                        directLookup = Aurora.rhash(result)
                        if directLookup then
                            directLookup = Aurora.escape(directLookup)
                        end
                    end
                end
            end

            ImGui.Text("0x%x", output)
            ImGui.SameLine()

            if ImGui.SmallButton("Copy") then
                ImGui.LogToClipboard()
                ImGui.LogText("%x", output)
			    ImGui.LogFinish()
			end

			if cacheHit then
				ImGui.Text("File found: %x.pc", output)
			end

			if rhashHit then
				ImGui.Text("Result found in hashtable: %s", rhashHit)
			end

			if directLookup then
				ImGui.Text("Direct hit found in hashtable: %s", directLookup)
			end
		end
	},
}