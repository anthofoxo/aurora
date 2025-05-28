local input = Aurora.box("")
local output = 0
local cacheHit = false

return {
	id = "aurora.hasher",
	gui = {
		visible = true,
		title = "Hasher",
		OnGui = function()
			if ImGui.InputText("Input", input) then
				output = Aurora.hash(Aurora.unbox(input))
				cacheHit = Aurora.cache_hit(string.format("%x.pc", output))
			end
			ImGui.Text("0x%x", output)

			if cacheHit then
				ImGui.Text("File found: %x.pc", output)
			end
		end
	},
}