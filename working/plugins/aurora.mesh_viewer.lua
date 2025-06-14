local knownMeshes = {}
local selection = nil
local contentRegionAvailLast = { 0, 0 }
local texture
local framebuffer
local renderbuffer
local vao
local vbo

vbo = gl.CreateBuffers()
local data = "\x00\x00\x00\xBF\x00\x00\x00\x3F\x00\x00\x00\xBF\x00\x00\x00\xBF\x00\x00\x00\x3F\x00\x00\x00\x3F\x00\x00\x00\x3F\x00\x00\x00\xBF"
gl.NamedBufferStorage(vbo, #data, data, GL.NONE)

vao = gl.CreateVertexArrays()
gl.VertexArrayVertexBuffer(vao, 0, vbo, 0, 8)
gl.VertexArrayAttribFormat(vao, 0, 2, GL.FLOAT, false, 0)
gl.VertexArrayAttribBinding(vao, 0, 0)
gl.EnableVertexArrayAttrib(vao, 0)

local vertShader = gl.CreateShader(GL.VERTEX_SHADER)
gl.ShaderSource(vertShader,
[[#version 450 core
layout (location = 0) in vec2 iPosition;
out vec3 vColor;

void main(void) {
    gl_Position = vec4(iPosition, 0.0, 1.0);
    vColor = vec3(iPosition + 0.5, 1.0);
}
]])
gl.CompileShader(vertShader)

local fragShader = gl.CreateShader(GL.FRAGMENT_SHADER)
gl.ShaderSource(fragShader,
[[#version 450 core
in vec3 vColor;

layout (location = 0) out vec4 oColor;

void main(void) {
    oColor = vec4(vColor, 1.0);
}
]])
gl.CompileShader(fragShader)

local program = gl.CreateProgram()
gl.AttachShader(program, vertShader)
gl.AttachShader(program, fragShader)
gl.LinkProgram(program)
gl.DetachShader(program, vertShader)
gl.DetachShader(program, fragShader)
gl.DeleteShader(vertShader)

for _, value in pairs(Aurora.hashtable()) do
    if string.match(value, ".x") then
        table.insert(knownMeshes, value)
    end
end

return {
    OnUnload = function()
        if texture ~= nil then
            gl.DeleteTextures(texture)
        end

        if framebuffer ~= nil then
            gl.DeleteFramebuffers(framebuffer)
        end

        if renderbuffer ~= nil then
            gl.DeleteRenderbuffers(renderbuffer)
        end

        gl.DeleteProgram(program)
        gl.DeleteVertexArrays(vao)
        gl.DeleteBuffers(vbo)
    end,
	gui = {
        title = "Mesh Viewer",
        OnGui = function()
            ImGui.Text("%d known meshes", #knownMeshes)
            ImGui.Separator()

            ImGui.Columns(2);

            if ImGui.BeginChild("MeshViewerScrollRegion") then
                for _, value in ipairs(knownMeshes) do
                    if ImGui.Selectable(Aurora.escape(value), selection == value) then
                        selection = value

                        -- Load and parse mesh data
                    end
                end
            end
            ImGui.EndChild()

            ImGui.NextColumn();

            local regionAvail = ImGui.GetContentRegionAvail()

            if contentRegionAvailLast[1] ~= regionAvail[1] or contentRegionAvailLast[2] ~= regionAvail[2] then
                contentRegionAvailLast = regionAvail
                -- Content region size has changed, recreate the texture

                if texture ~= nil then
                    gl.DeleteTextures(texture)
                end

                texture = gl.CreateTextures(GL.TEXTURE_2D)
                gl.TextureStorage2D(texture, 1, GL.RGBA8, regionAvail[1], regionAvail[2])

                if renderbuffer ~= nil then
                    gl.DeleteRenderbuffers(renderbuffer)
                end

                renderbuffer = gl.CreateRenderbuffers()
                gl.NamedRenderbufferStorage(renderbuffer, GL.DEPTH_COMPONENT32, regionAvail[1], regionAvail[2])

                if framebuffer ~= nil then
                    gl.DeleteFramebuffers(framebuffer)
                end

                framebuffer = gl.CreateFramebuffers()
                gl.NamedFramebufferTexture(framebuffer, GL.COLOR_ATTACHMENT0, texture, 0)
                gl.NamedFramebufferRenderbuffer(framebuffer, GL.DEPTH_ATTACHMENT, renderbuffer)
            end

            gl.BindFramebuffer(GL.FRAMEBUFFER, framebuffer)
            gl.Viewport(0, 0, regionAvail[1], regionAvail[2])
            gl.ClearNamedFramebufferfv(framebuffer, GL.COLOR, 0, { 0.7, 0.8, 0.9, 1.0 })
            gl.ClearNamedFramebufferfv(framebuffer, GL.DEPTH, 0, { 1.0 })
            gl.UseProgram(program)
            gl.BindVertexArray(vao)
            gl.DrawArrays(GL.TRIANGLE_STRIP, 0, 4)

            ImGui.Image(texture, regionAvail, { 0.0, 1.0 }, { 1.0, 0.0 })
        end
	},
}