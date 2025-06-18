local knownMeshes = {}
local selection = nil
local contentRegionAvailLast = { 0, 0 }
local texture
local framebuffer
local renderbuffer
local vao
local vbo
local ebo
local count
local geomCenter = { 0, 0, 0 }
local largestVertexCoord = 0
local errormsg

local program = Aurora.util.create_shader_program(
[[#version 450 core
layout (location = 0) in vec3 iPosition;
layout (location = 1) in vec3 iNormal;
out vec3 vNormal;

uniform mat4 uProjection;
uniform mat4 uView;

void main(void) {
 gl_Position = uProjection * uView * vec4(iPosition, 1.0);
 vNormal = iNormal;
}
]],
[[#version 450 core
in vec3 vNormal;

layout (location = 0) out vec4 oColor;

void main(void) {
 oColor = vec4(1.0, 1.0, 1.0, 1.0);
 oColor.rgb *= max(dot(vec3(0.0, 0.0, 1.0), normalize(vNormal)), 0.2);
}
]])

local function read_u32(bytes, ptr)
    return (bytes:byte(ptr + 0) << 0) | (bytes:byte(ptr + 1) << 8) | (bytes:byte(ptr + 2) << 16) | (bytes:byte(ptr + 3) << 24)
end

local function render()
    local regionAvail = ImGui.GetContentRegionAvail()

    if contentRegionAvailLast[1] ~= regionAvail[1] or contentRegionAvailLast[2] ~= regionAvail[2] then
        contentRegionAvailLast = regionAvail

        if texture then
            gl.DeleteTextures(texture)
        end

        texture = gl.CreateTextures(GL.TEXTURE_2D)
        gl.TextureStorage2D(texture, 1, GL.RGBA8, regionAvail[1], regionAvail[2])

        if renderbuffer then
            gl.DeleteRenderbuffers(renderbuffer)
        end

        renderbuffer = gl.CreateRenderbuffers()
        gl.NamedRenderbufferStorage(renderbuffer, GL.DEPTH_COMPONENT32, regionAvail[1], regionAvail[2])

        if framebuffer then
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

    if vao then
        gl.UseProgram(program)
        gl.BindVertexArray(vao)

        local projection = glm.perspective(math.rad(90.0), regionAvail[1] / regionAvail[2], 0.1, 2048.0)
        local projectionLocation = gl.GetUniformLocation(program, "uProjection")
        gl.ProgramUniformMatrix4fv(program, projectionLocation, 1, false, projection)

        local view = glm.lookAt({ largestVertexCoord, largestVertexCoord, largestVertexCoord }, geomCenter, { 0, 1, 0 })
        local viewLocation = gl.GetUniformLocation(program, "uView")
        gl.ProgramUniformMatrix4fv(program, viewLocation, 1, false, view)

        gl.DrawElements(GL.TRIANGLES, count, GL.UNSIGNED_SHORT, 0)
    end

    ImGui.Image(texture, regionAvail, { 0.0, 1.0 }, { 1.0, 0.0 })
end

local function ActionOpen(filepath)
    errormsg = nil
    selection = filepath

    if vbo then gl.DeleteBuffers(vbo) end
    if vao then gl.DeleteVertexArrays(vao) end
    if ebo then gl.DeleteBuffers(ebo) end

    vao = nil
    vbo = nil
    ebo = nil

    local status, result = pcall(function()
        local filebytes = Aurora.read_file(filepath)
        if filebytes then
            local ptr = 1

            local header = read_u32(filebytes, ptr); ptr = ptr + 4

            if header == 6 then
                local meshcount = read_u32(filebytes, ptr); ptr = ptr + 4

                if meshcount > 0 then
                    local vertexcount = read_u32(filebytes, ptr); ptr = ptr + 4

                    local vertexdata = string.sub(filebytes, ptr)

                    geomCenter = { 0, 0, 0 }
                    largestVertexCoord = 0

                    for i = 0, vertexcount - 1 do
                        local x = Aurora.bitcast_float(read_u32(filebytes, ptr + (i * 36) + 0))
                        local y = Aurora.bitcast_float(read_u32(filebytes, ptr + (i * 36) + 4))
                        local z = Aurora.bitcast_float(read_u32(filebytes, ptr + (i * 36) + 8))

                        geomCenter[1] = geomCenter[1] + x
                        geomCenter[2] = geomCenter[2] + y
                        geomCenter[3] = geomCenter[3] + z

                        x = math.abs(x)
                        y = math.abs(y)
                        z = math.abs(z)

                        if x > largestVertexCoord then largestVertexCoord = x end
                        if y > largestVertexCoord then largestVertexCoord = y end
                        if z > largestVertexCoord then largestVertexCoord = z end
                    end

                    geomCenter[1] = geomCenter[1] / vertexcount
                    geomCenter[2] = geomCenter[2] / vertexcount
                    geomCenter[3] = geomCenter[3] / vertexcount

                    largestVertexCoord = largestVertexCoord * 1.5

                    vbo = gl.CreateBuffers()
                    gl.NamedBufferStorage(vbo, vertexcount * 36, vertexdata, GL.NONE)

                    ptr = ptr + vertexcount * 36

                    -- element list
                    local trianglecount = read_u32(filebytes, ptr); ptr = ptr + 4
                    count = trianglecount * 3
                    -- ptr is now at a location that references the triangle/element list
                    local elementdata = string.sub(filebytes, ptr)

                    ebo = gl.CreateBuffers()
                    gl.NamedBufferStorage(ebo, trianglecount * 6, elementdata, GL.NONE)

                    vao = gl.CreateVertexArrays()
                    gl.VertexArrayVertexBuffer(vao, 0, vbo, 0, 36)
                    gl.VertexArrayElementBuffer(vao, ebo)
                    gl.VertexArrayAttribFormat(vao, 0, 3, GL.FLOAT, false, 0)
                    gl.VertexArrayAttribFormat(vao, 1, 3, GL.FLOAT, false, 12)
                    gl.VertexArrayAttribBinding(vao, 0, 0)
                    gl.VertexArrayAttribBinding(vao, 1, 0)
                    gl.EnableVertexArrayAttrib(vao, 0)
                    gl.EnableVertexArrayAttrib(vao, 1)
                end
            else
                errormsg = "File is not a mesh"
            end
        end
    end)
    if status == false then
        errormsg = result
    end
end

return {
    OnMessageRecieved = function(source, action, data)
        if action == "open" then ActionOpen(data.file)
        else print("unknown action: " .. action) end
    end,

    OnUnload = function()
        if program then
            gl.DeleteProgram(program)
        end

        if texture ~= nil then
            gl.DeleteTextures(texture)
        end

        if framebuffer ~= nil then
            gl.DeleteFramebuffers(framebuffer)
        end

        if renderbuffer ~= nil then
            gl.DeleteRenderbuffers(renderbuffer)
        end

        if vao then
            gl.DeleteVertexArrays(vao)
        end

        if vbo then
            gl.DeleteBuffers(vbo)
        end

        if ebo then
            gl.DeleteBuffers(ebo)
        end
    end,
	gui = {
        title = "Mesh Viewer",
        OnGui = function()
            if selection then
                ImGui.TextUnformatted(selection)
                ImGui.TextUnformatted(Aurora.escape(Aurora.rhash(tonumber(Aurora.filesystem.stem(selection), 16)) or "???"))
            end
            if errormsg then ImGui.TextUnformatted(errormsg) end
            render()
        end
	},
}