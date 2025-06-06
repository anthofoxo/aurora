---@meta Aurora

---@generic T
---@class Box<T>

Aurora = {
    ---@param value string
    ---@return string[]?
    ---@native
    directory_iterator = function(value) end,

    ---@param value string
    ---@return boolean
    ---@native
    is_regular_file = function(value) end,

    ---@param value string
    ---@return number
    ---@native
    hash = function(value) end,

    ---@param value number
    ---@return string?
    ---@native
    rhash = function(value) end,

    ---@param value string
    ---@return boolean
    ---@native
    cache_hit = function(value) end,

    ---@generic T
    ---@param value T
    ---@return Box<T>
    box = function(value) end,

    ---@generic T
    ---@param value Box<T>
    ---@return T
    unbox = function(value) end,

    ---@param value string
    ---@return string
    ---@native
    unescape = function(value) end,

    ---@param value string
    ---@return string
    ---@native
    escape = function(value) end,
}

ImGui = {
	Begin = function(label) end,
	End = function() end,
	BeginMainMenuBar = function() end,
	EndMainMenuBar = function() end,
	BeginMenu = function() end,
	EndMenu = function() end,
	SeparatorText = function() end,
	TextUnformatted = function() end,

    ---@param label string
    ---@param text Box<string>
	InputText = function(label, text) end,
    
	Text = function() end,
	BulletText = function() end,

    ---@param fmt string
    ---@param ... any
	TextWrapped = function(fmt, ...) end,

	MenuItem = function() end,
}