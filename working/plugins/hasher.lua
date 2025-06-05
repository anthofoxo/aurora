local input = Aurora.box("")
local output = 0
local cacheHit = false
local rhashHit = nil

return {
	id = "aurora.hasher",
	gui = {
		visible = true,
		title = "Hasher",
		OnGui = function()
			if ImGui.InputText("Input", input) then
				local unescaped = Aurora.unescape(Aurora.unbox(input))
				output = Aurora.hash(unescaped)
				cacheHit = Aurora.cache_hit(string.format("%x.pc", output))
				rhashHit = Aurora.rhash(output)
			end
			ImGui.Text("0x%x", output)

			if cacheHit then
				ImGui.Text("File found: %x.pc", output)
			end

			if rhashHit then
				ImGui.Text("Result found in hashtable: %s", rhashHit)
			end
		end
	},
}