---@meta Aurora

---@generic T
---@class Box<T>

---@alias Ref<T> T[]

---@alias mat4 number[]

Aurora = {
    filesystem = {
        ---@param value string
        ---@return string[]?
        directory_iterator = function(value) end,

        ---@param value string
        ---@return boolean
        is_regular_file = function(value) end,

        ---@param path string
        ---@return boolean
        create_directory = function(path) end,

        ---@param path string
        ---@return boolean
        create_directories = function(path) end,

        ---@param path string
        ---@return boolean
        exists = function(path) end,

        ---@param path string
        ---@return string
        stem = function(path) end,
    },

    ---@param value integer
    ---@return number
    ---@nodiscard
    bitcast_float = function(value) end,

    ---@param path string
    ---@return string?
    read_file = function(path) end,

    ---@param path string
    ---@param data string
    ---@return boolean
    write_file = function(path, data) end,

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

    ---Takes DDS/KTX binary data and loads them into a gpu texture
    ---The returned texture MUST be freed by the user
    ---@param bytes string
    ---@return integer
    ddsktx_parse = function(bytes) end,
}

---@class MemoryBuffer

function MemoryBuffer:reserve(size) end

MemoryBuffer = {
    ---@return MemoryBuffer
    new = function() end,


    --@param self MemoryBuffer
    --@param size integer
    ---reserve = function(self, size) end,

    ---@param self MemoryBuffer
    ---@return integer
    cursor = function(self) end,

    ---@param self MemoryBuffer
    ---@return integer
    size = function(self) end,

    ---@param self MemoryBuffer
    ---@return integer
    capacity = function(self) end,

    ---@param self MemoryBuffer
    close = function(self) end,
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

    ---@param label string
    ---@return boolean
    ---@nodiscard
	MenuItem = function(label) end,

    Separator = function() end,

    ---@param image _Texture
    ---@param size table
    ---@param uv0 table?
	---@param uv1 table?
    Image = function(image, size, uv0, uv1) end,

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

    LogToClipboard = function() end,
    LogFinish = function() end,

    ---@param fmt string
    ---@param ... any
    LogText = function(fmt, ...) end,

    ---@return table
    GetContentRegionAvail = function() end,

    ---@return boolean
    ---@nodiscard
    BeginPopupContextItem = function() end,

    EndPopup = function() end,

    CloseCurrentPopup = function() end,

    ---@param text any
    SetItemTooltip = function(text) end,
}

glm = {
    ---@param fovy number
    ---@param aspect number
    ---@param near number
    ---@param far number
    ---@return mat4
    ---@nodiscard
    perspective = function(fovy, aspect, near, far) end,

    ---@param eye table
    ---@param center table
    ---@param up table
    ---@return number[]
    ---@nodiscard
    lookAt = function(eye, center, up) end,
}

---@generic T
---@class Pointer<T>
