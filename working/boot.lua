-------------------------
-- Modding API Runtime --
--    DO NOT MODIFY    --
-------------------------

assert(Aurora)
assert(Aurora.directory_iterator)
assert(Aurora.is_regular_file)
Aurora.box = function(value) return { value } end
Aurora.unbox = function(value) return rawget(value, 1) end

function string.endswith(input, affix) return string.sub(input, -#affix) == affix end

local plugins = {}

local pluginCount = 0
print("Loading plugins...")

for _, entry in ipairs(Aurora.directory_iterator("plugins") or {}) do
	if Aurora.is_regular_file(entry) and entry:endswith(".lua") then
		local status, result = pcall(function()
			local plugin = dofile(entry)
			assert(plugin)
			assert(plugin.id)
			plugin.enabled = true

			if plugin.gui then
				plugin.gui.visible = plugin.gui.visible or false
			end

			plugins[plugin.id] = plugin

			print(plugin.id)
			pluginCount = pluginCount + 1
		end)

		if not status then
			print(string.format("Error loading plugin %s: %s", entry, result))
		end
	end
end

print(string.format("Loaded %d plugins", pluginCount))

return {
    plugins = plugins,
	-- OnUpdate = function() end
}