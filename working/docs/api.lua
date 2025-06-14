---@meta Aurora

---@generic T
---@class Box<T>

---@alias Ref<T> T[]

---@alias mat4 number[]

---@alias GLenum integer
---@alias GLint integer
---@alias GLuint integer
---@alias GLsizei integer
---@alias GLsizeiptr integer
---@alias GLintptr integer
---@alias GLbitfield integer
---@alias GLboolean boolean

---@class _Texture[:GLuint]
---@class _Renderbuffer[:GLuint]
---@class _Framebuffer[:GLuint]
---@class _Buffer[:GLuint]
---@class _VertexArray[:GLuint]
---@class _Shader[:GLuint]
---@class _Program[:GLuint]

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
}

glm = {
    ---@param fovy number
    ---@param aspect number
    ---@param near number
    ---@param far number
    ---@return mat4
    perspective = function(fovy, aspect, near, far) end,
}

gl = {
    ---@return _Buffer
	---@nodiscard
	CreateBuffers = function() end,

	---@param buffer _Buffer
    DeleteBuffers = function(buffer) end,

    ---@return _VertexArray
    ---@nodiscard
    CreateVertexArrays = function() end,

    ---@param array _VertexArray
    DeleteVertexArrays = function(array) end,

	---@param buffer _Buffer
	---@param size GLsizeiptr
	---@param data string
	---@param flags GLbitfield
	NamedBufferStorage = function(buffer, size, data, flags) end,

    ---@param target GLenum
    ---@return _Texture
	---@nodiscard
    CreateTextures = function(target) end,

	---@param texture _Texture
    DeleteTextures = function(texture) end,

    ---@param texture _Texture
    ---@param levels GLsizei
    ---@param internalFormat GLenum
    ---@param width GLsizei
    ---@param height GLsizei
    TextureStorage2D = function(texture, levels, internalFormat, width, height) end,

    ---@param target GLenum
    ---@param framebuffer _Framebuffer
    BindFramebuffer = function(target, framebuffer) end,

    ---@return _Framebuffer
    ---@nodiscard
    CreateFramebuffers = function() end,

    ---@param framebuffer _Framebuffer
    DeleteFramebuffers = function(framebuffer) end,

    ---@return _Renderbuffer
    ---@nodiscard
    CreateRenderbuffers = function() end,

    ---@param renderbuffer _Renderbuffer
    DeleteRenderbuffers = function(renderbuffer) end,

    ---@param framebuffer _Framebuffer
    ---@param attachment GLenum
    ---@param texture _Texture
    ---@param level GLint
    NamedFramebufferTexture = function(framebuffer, attachment, texture, level) end,

    ---@param renderbuffer _Renderbuffer
    ---@param internalFormat GLenum
    ---@param width GLsizei
    ---@param height GLsizei
    NamedRenderbufferStorage = function(renderbuffer, internalFormat, width, height) end,

    ---@param framebuffer _Framebuffer
    ---@param attachment GLenum
    ---@param renderbuffer _Renderbuffer
    NamedFramebufferRenderbuffer = function(framebuffer, attachment, renderbuffer) end,

    ---@param x GLint
    ---@param y GLint
    ---@param width GLsizei
    ---@param height GLsizei
    Viewport = function(x, y, width, height) end,

    ---@param framebuffer _Framebuffer
    ---@param buffer GLenum
    ---@param drawbuffer GLint
    ---@param value table
    ClearNamedFramebufferfv = function(framebuffer, buffer, drawbuffer, value) end,

    ---@param array _VertexArray
    BindVertexArray = function(array) end,

    ---@param vaobj _VertexArray
    ---@param bindingindex GLuint
    ---@param buffer _Buffer
    ---@param offset GLintptr
    ---@param stride GLsizei
    VertexArrayVertexBuffer = function(vaobj, bindingindex, buffer, offset, stride) end,

    ---@param vaobj _VertexArray
    ---@param index GLuint
    EnableVertexArrayAttrib = function(vaobj, index) end,

    ---@param vaobj _VertexArray
    ---@param attribindex GLuint
    ---@param size GLint
    ---@param type GLenum
    ---@param normalized GLboolean
    ---@param relativeoffset GLuint
    VertexArrayAttribFormat = function(vaobj, attribindex, size, type, normalized, relativeoffset) end,

    ---@param vaobj _VertexArray
    ---@param attribindex GLuint
    ---@param bindingindex GLuint
    VertexArrayAttribBinding = function(vaobj, attribindex, bindingindex) end,

    ---@param mode GLenum
    ---@param first GLint
    ---@param count GLsizei
    DrawArrays = function(mode, first, count) end,

    ---@param type GLenum
    ---@return _Shader
    ---@nodiscard
    CreateShader = function(type) end,

    ---@param shader _Shader
    DeleteShader = function(shader) end,

    ---@param program _Program
    ---@param shader _Shader
    AttachShader = function(program, shader) end,

    ---@param program _Program
    ---@param shader _Shader
    DetachShader = function(program, shader) end,

    ---@param shader _Shader
    ---@param source string
    ShaderSource = function(shader, source) end,

    ---@param shader _Shader
    CompileShader = function(shader) end,

    ---@return _Program
    ---@nodiscard
    CreateProgram = function() end,

    ---@param program _Program
    LinkProgram = function(program) end,

    ---@param program _Program
    UseProgram = function(program) end,

    ---@param program _Program
    DeleteProgram = function(program) end,

    ---@param program _Program
    ---@param name string
    ---@return GLint
    ---@nodiscard
    GetUniformLocation = function(program, name) end,

    ---@param program _Program
    ---@param location GLint
    ---@param count GLsizei
    ---@param transpose GLboolean
    ---@param value Ref<number>
    ProgramUniformMatrix4fv = function(program, location, count, transpose, value) end,
}

GL = {
    TEXTURE_2D = 0,
    RGBA8 = 0,
    FRAMEBUFFER = 0,
    COLOR_ATTACHMENT0 = 0,
    COLOR_ATTACHMENT1 = 0,
    COLOR_ATTACHMENT2 = 0,
    COLOR_ATTACHMENT3 = 0,
    COLOR_ATTACHMENT4 = 0,
    COLOR_ATTACHMENT5 = 0,
    COLOR_ATTACHMENT6 = 0,
    COLOR_ATTACHMENT7 = 0,
    DEPTH_ATTACHMENT = 0,
    DEPTH_COMPONENT24 = 0,
    DEPTH_COMPONENT32 = 0,
    DEPTH_COMPONENT32F = 0,
    COLOR = 0,
    DEPTH = 0,
    STENCIL = 0,
    NONE = 0,
    FLOAT = 0,
    TRIANGLE_STRIP = 0,
    VERTEX_SHADER = 0,
    FRAGMENT_SHADER = 0,
}