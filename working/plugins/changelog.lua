local changelogs = {
	{
		version = "v0.0.4-a.2+wip",
		items = {
			"Updated font files to noto sans",
			"Hex viewer how has a monospace font",
			"Linux Support",
			"Fixed crash when attempting to open hex viewer for `exe` binary search results",
			"Fixed crash when unpacked exe doesn't exist",
			"Hashtable support",
			"Binary hasher will lookup results in the hashtable to display results",
			"Improved crash handling",
			"Expanded scripting api with imgui support",
			"Plugin Support",
		},
	},
	{
		version = "v0.0.4-a.1",
		items = {
			"Cache binary search",
			"General purpose hasher",
			"Hasher informs you when the hash matches a .pc file",
			"Scripting with exposed hash function",
		},
	}
}


local general = "Aurora v0.0.4+ is being rebuilt from the ground up with a focus on usability and modularity. This version of Aurora DOES NOT have feature parity with older versions."

return {
	id = "aurora.changelog",
	gui = {
		title = "Changelog",
		OnGui = function()
			ImGui.TextWrapped("%s", general)

			for _, changelog in ipairs(changelogs) do
				ImGui.SeparatorText(changelog.version)

				for _, item in ipairs(changelog.items) do
					ImGui.BulletText(item)
				end
			end
		end
	},
}