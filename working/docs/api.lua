---@meta Aurora

---@generic T
---@class Box<T>

Aurora = {
    ---@param path string
    ---@return string?
    read_file = function(path) end,

    ---@param path string
    ---@param data string
    ---@return boolean
    write_file = function(path, data) end,

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

    ---@return string
    game_directory = function() end,

    ---@param path string
    ---@return boolean
    create_directory = function(path) end,

    ---@param path string
    ---@return boolean
    create_directories = function(path) end,


    ---Takes DDS/KTX binary data and loads them into a gpu texture
    ---The returned texture MUST be freed by the user
    ---@param bytes string
    ---@return integer
    ddsktx_parse = function(bytes) end,
}

ImGui = {
	BeginMenu = function() end,
	EndMenu = function() end,

    ---@param style integer
    ---@param value table|number
    PushStyleVar = function(style, value) end,

    PopStyleVar = function() end,

    TextColored = function(color, fmt, ...) end,

    SameLine = function() end,

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

    ---@param text string
    ---@param selected boolean?
    ---@return boolean
    Selectable = function(text, selected) end,

	MenuItem = function() end,

    Separator = function() end,

    ---@param image integer
	---@param size table
    Image = function(image, size) end,

	---@param id string
	---@param cols integer
    BeginTable = function(id, cols) end,

    EndTable = function() end,

    TableNextRow = function() end,

    ---@param idx integer
    TableSetColumnIndex = function(idx) end,

	---@param cols integer
    Columns = function(cols) end,

    NextColumn = function() end,

	---@param id string
    BeginChild = function(id) end,

	EndChild = function() end,
}

gl = {
    ---@return integer
	---@nodiscard
	CreateBuffers = function() end,

	---@param buffer integer
    DeleteBuffers = function(buffer) end,

	---@param buffer integer
	---@param size integer
	---@param data string
	---@param flags integer
	NamedBufferStorage = function(buffer, size, data, flags) end,

    ---@return integer
	---@nodiscard
    CreateVertexArrays = function() end,

    ---@param target integer
    ---@return integer
	---@nodiscard
    CreateTextures = function(target) end,

	---@param texture integer
    DeleteTextures = function(texture) end,

    ---@param texture integer
    ---@param levels integer
    ---@param internalFormat integer
    ---@param width integer
    ---@param height integer
    TextureStorage2D = function(texture, levels, internalFormat, width, height) end,
}

GL = {
    TEXTURE_2D = 0,
    RGBA8 = 0,
}