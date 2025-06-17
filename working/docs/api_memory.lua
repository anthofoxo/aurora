---@meta Aurora

Memory = {
    ---@param size integer
    ---@return lightuserdata
    malloc = function(size) end,

    ---@param ptr lightuserdata
    free = function(ptr) end,

    ---@param ptr lightuserdata
    ---@param offset integer
    ---@return lightuserdata
    offset = function(ptr, offset) end,

    ---@return lightuserdata
    null = function() end,

    ---@param ptr lightuserdata
    ---@return integer
    read_u32 = function(ptr) end,

    ---@param ptr lightuserdata
    ---@param value integer
    write_u32 = function(ptr, value) end,

    ---@param ptr lightuserdata
    ---@param value lightuserdata
    write_ptr = function(ptr, value) end,

    ---@param ptr lightuserdata
    ---@param data string
    ---@param size integer
    write_bytes = function(ptr, data, size) end,
}