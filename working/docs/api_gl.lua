---@meta Aurora

gl = {
    ---@param n integer `GLsizei`
    ---@param arrays lightuserdata `GLuint*`
    CreateVertexArrays = function(n, arrays) end,

    ---@param n integer `GLsizei`
    ---@param arrays lightuserdata `GLuint const*`
    DeleteVertexArrays = function(n, arrays) end,

    ---@param array integer `GLuint`
    BindVertexArray = function(array) end,

    ---@param vaobj integer `GLuint`
    ---@param bindingindex integer `GLuint`
    ---@param buffer integer `GLuint`
    ---@param offset integer `GLintptr`
    ---@param stride integer `GLsizei`
    VertexArrayVertexBuffer = function(vaobj, bindingindex, buffer, offset, stride) end,

    ---@param vaobj integer `GLuint`
    ---@param buffer integer `GLuint`
    VertexArrayElementBuffer = function(vaobj, buffer) end,

    ---@param vaobj integer `GLuint`
    ---@param index integer `GLuint`
    EnableVertexArrayAttrib = function(vaobj, index) end,

    ---@param vaobj integer `GLuint`
    ---@param attribindex integer `GLuint`
    ---@param size integer `GLint`
    ---@param type integer `GLenum`
    ---@param normalized boolean `GLboolean`
    ---@param relativeoffset integer `GLuint`
    VertexArrayAttribFormat = function(vaobj, attribindex, size, type, normalized, relativeoffset) end,

    ---@param vaobj integer `GLuint`
    ---@param attribindex integer `GLuint`
    ---@param bindingindex integer `GLuint`
    VertexArrayAttribBinding = function(vaobj, attribindex, bindingindex) end,

    ---@param n integer `GLsizei`
    ---@param buffers lightuserdata `GLuint*`
    CreateBuffers = function(n, buffers) end,

    ---@param n integer `GLsizei`
    ---@param buffers lightuserdata `GLuint const*`
    DeleteBuffers = function(n, buffers) end,

    ---@param buffer integer `GLuint`
    ---@param size integer `GLsizeiptr`
    ---@param data lightuserdata `void const*`
    ---@param flags integer `GLbitfield`
    NamedBufferStorage = function(buffer, size, data, flags) end,

    ---@param shaderType integer `GLenum`
    ---@return integer `GLuint`
    CreateShader = function(shaderType) end,

    ---@param shader integer `GLuint`
    DeleteShader = function(shader) end,

    ---@param shader integer `GLuint`
    ---@param count integer `GLsizei`
    ---@param string lightuserdata `GLchar const**`
    ---@param length lightuserdata `GLint const*`
    ShaderSource = function(shader, count, string, length) end,

    ---@param shader integer `GLuint`
    CompileShader = function(shader) end,

    ---@return integer `GLuint`
    CreateProgram = function() end,

    ---@param program integer `GLuint`
    DeleteProgram = function(program) end,

    ---@param program integer `GLuint`
    UseProgram = function(program) end,

    ---@param program integer `GLuint`
    ---@param shader integer `GLuint`
    AttachShader = function(program, shader) end,

    ---@param program integer `GLuint`
    ---@param shader integer `GLuint`
    DetachShader = function(program, shader) end,

    ---@param program integer `GLuint`
    LinkProgram = function(program) end,

    ---@param program integer `GLuint`
    ---@param name lightuserdata|string `GLchar const*`
    ---@return integer `GLint`
    GetUniformLocation = function(program, name) end,

    ---@param program integer `GLuint`
    ---@param location integer `GLint`
    ---@param count integer `GLsizei`
    ---@param transpose boolean `GLboolean`
    ---@param value lightuserdata|number[] `GLfloat const*`
    ProgramUniformMatrix4fv = function(program, location, count, transpose, value) end,

    ---@param target integer `GLenum`
    ---@param n integer `GLsizei`
    ---@param textures lightuserdata `GLuint*`
    CreateTextures = function(target, n, textures) end,

    ---@param n integer `GLsizei`
    ---@param textures lightuserdata `GLuint const*`
    DeleteTextures = function(n, textures) end,

    ---@param texture integer `GLuint`
    ---@param levels integer `GLsizei`
    ---@param internalFormat integer `GLenum`
    ---@param width integer `GLsizei`
    ---@param height integer `GLsizei`
    TextureStorage2D = function(texture, levels, internalFormat, width, height) end,

    ---@param n integer `GLsizei`
    ---@param renderbuffers lightuserdata `GLuint*`
    CreateRenderbuffers = function(n, renderbuffers) end,

    ---@param n integer `GLsizei`
    ---@param renderbuffers lightuserdata `GLuint const*`
    DeleteRenderbuffers = function(n, renderbuffers) end,

    ---@param renderbuffer integer `GLuint`
    ---@param internalFormat integer `GLenum`
    ---@param width integer `GLsizei`
    ---@param height integer `GLsizei`
    NamedRenderbufferStorage = function(renderbuffer, internalFormat, width, height) end,

    ---@param n integer `GLsizei`
    ---@param framebuffers lightuserdata `GLuint*`
    CreateFramebuffers = function(n, framebuffers) end,

    ---@param n integer `GLsizei`
    ---@param framebuffers lightuserdata `GLuint const*`
    DeleteFramebuffers = function(n, framebuffers) end,

    ---@param target integer `GLenum`
    ---@param framebuffer integer `GLuint`
    BindFramebuffer = function(target, framebuffer) end,

    ---@param framebuffer integer `GLuint`
    ---@param attachment integer `GLenum`
    ---@param texture integer `GLuint`
    ---@param level integer `GLint`
    NamedFramebufferTexture = function(framebuffer, attachment, texture, level) end,

    ---@param framebuffer integer `GLuint`
    ---@param attachment integer `GLenum`
    ---@param renderbuffertarget integer `GLenum`
    ---@param renderbuffer integer `GLuint`
    NamedFramebufferRenderbuffer = function(framebuffer, attachment, renderbuffertarget, renderbuffer) end,

    ---@param framebuffer integer `GLuint`
    ---@param buffer integer `GLenum`
    ---@param drawbuffer integer `GLint`
    ---@param value lightuserdata|number[] `GLfloat const*`
    ClearNamedFramebufferfv = function(framebuffer, buffer, drawbuffer, value) end,

    ---@param x integer `GLint`
    ---@param y integer `GLint`
    ---@param width integer `GLsizei`
    ---@param height integer `GLsizei`
    Viewport = function(x, y, width, height) end,

    ---@param mode integer `GLenum`
    ---@param first integer `GLint`
    ---@param count integer `GLsizei`
    DrawArrays = function(mode, first, count) end,

    ---@param mode integer `GLenum`
    ---@param count integer `GLsizei`
    ---@param type integer `GLenum`
    ---@param indices lightuserdata `void const*`
    DrawElements = function(mode, count, type, indices) end,
}

GL = {
    COLOR = 0,
    COLOR_ATTACHMENT0 = 0,
    COLOR_ATTACHMENT1 = 0,
    COLOR_ATTACHMENT2 = 0,
    COLOR_ATTACHMENT3 = 0,
    COLOR_ATTACHMENT4 = 0,
    COLOR_ATTACHMENT5 = 0,
    COLOR_ATTACHMENT6 = 0,
    COLOR_ATTACHMENT7 = 0,
    DEPTH = 0,
    DEPTH_ATTACHMENT = 0,
    DEPTH_COMPONENT24 = 0,
    DEPTH_COMPONENT32 = 0,
    DEPTH_COMPONENT32F = 0,
    FLOAT = 0,
    FRAGMENT_SHADER = 0,
    FRAMEBUFFER = 0,
    NONE = 0,
    RENDERBUFFER = 0,
    RGBA8 = 0,
    STENCIL = 0,
    TEXTURE_2D = 0,
    TRIANGLES = 0,
    TRIANGLE_STRIP = 0,
    UNSIGNED_INT = 0,
    UNSIGNED_SHORT = 0,
    VERTEX_SHADER = 0,
}