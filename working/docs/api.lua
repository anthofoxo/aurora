---@meta Aurora

---@generic T
---@class Box<T>

Aurora = {
    ---@param value string
    ---@return string[]?
    directory_iterator = function(value) end,

    ---@param value string
    ---@return boolean
    is_regular_file = function(value) end,

    ---@param value string
    ---@return number
    hash = function(value) end,

    ---@param value number
    ---@return string?
    rhash = function(value) end,

    ---@param value string
    ---@return boolean
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
    unescape = function(value) end,

    ---@param value string
    ---@return string
    escape = function(value) end,
}

ImGui = {
	BeginMenu = function() end,
	EndMenu = function() end,
	
    ---@param text string
	TextUnformatted = function(text) end,

    ---@param fmt string
    ---@param ... any
    Text = function(fmt, ...) end,

    ---@param fmt string
    ---@param ... any
	TextWrapped = function(fmt, ...) end,

    ---@param label string
    ---@param fmt string
    ---@param ... any
    LabelText = function(label, fmt, ...) end,

    ---@param fmt string
    ---@param ... any
    BulletText = function(fmt, ...) end,

    ---@param label string
    SeparatorText = function(label) end,

    
	
    ---@param label string
    ---@param text Box<string>
	InputText = function(label, text) end,

    

	MenuItem = function() end,

	Separator = function() end,
}