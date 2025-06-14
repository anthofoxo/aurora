-------------------------
-- Modding API Runtime --
--    DO NOT MODIFY    --
-------------------------

assert(Aurora)
Aurora.box = function(value) return { value } end
Aurora.unbox = function(value) return rawget(value, 1) end

local function tablelength(T)
  local count = 0
  for _ in pairs(T) do count = count + 1 end
  return count
end

print("Loading hashtable...")
local hashtable = dofile("hashtable.lua")
Aurora.hashtable = function() return hashtable end
print(string.format("Loaded %d hashes", tablelength(hashtable)))

function string.endswith(input, affix) return string.sub(input, -#affix) == affix end

local plugins = {}

local pluginCount = 0
print("Loading plugins...")

Aurora.util = {
    ---@param vertSource string
    ---@param fragSource string
    ---@return _Program
    create_shader_program = function(vertSource, fragSource)
        local vertShader = gl.CreateShader(GL.VERTEX_SHADER)
        gl.ShaderSource(vertShader, vertSource)
        gl.CompileShader(vertShader)

        local fragShader = gl.CreateShader(GL.FRAGMENT_SHADER)
        gl.ShaderSource(fragShader, fragSource)
        gl.CompileShader(fragShader)

        local program = gl.CreateProgram()
        gl.AttachShader(program, vertShader)
        gl.AttachShader(program, fragShader)
        gl.LinkProgram(program)
        gl.DetachShader(program, vertShader)
        gl.DetachShader(program, fragShader)
        gl.DeleteShader(vertShader)
        gl.DeleteShader(fragShader)

        return program
    end,
}

for _, entry in ipairs(Aurora.filesystem.directory_iterator("plugins") or {}) do
	if Aurora.filesystem.is_regular_file(entry) and entry:endswith(".lua") then
		local status, result = pcall(function()
			local plugin = dofile(entry)
            assert(plugin)

			local pluginId = Aurora.filesystem.stem(entry)

			plugin.enabled = true

			if plugin.gui then
				plugin.gui.visible = plugin.gui.visible or false
			end

			plugins[pluginId] = plugin

			print(pluginId)
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
    hashtable = hashtable,
}