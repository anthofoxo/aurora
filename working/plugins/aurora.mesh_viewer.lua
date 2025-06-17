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
local program

local nativeblock = Memory.malloc(4)

local loaded = false
local function load()
    loaded = true

    for _, value in pairs(Aurora.hashtable()) do
        if string.match(value, "%.x") then
            table.insert(knownMeshes, value)
        end
    end

    program = Aurora.util.create_shader_program(
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
end -- load

local function read_u32(bytes, ptr)
    return (bytes:byte(ptr + 0) << 0) | (bytes:byte(ptr + 1) << 8) | (bytes:byte(ptr + 2) << 16) | (bytes:byte(ptr + 3) << 24)
end



local function render()


    local regionAvail = ImGui.GetContentRegionAvail()

    if contentRegionAvailLast[1] ~= regionAvail[1] or contentRegionAvailLast[2] ~= regionAvail[2] then
        contentRegionAvailLast = regionAvail

        if texture then
            Memory.write_u32(nativeblock, texture)
            gl.DeleteTextures(1, nativeblock)
        end

        gl.CreateTextures(GL.TEXTURE_2D, 1, nativeblock)
        texture = Memory.read_u32(nativeblock)

        gl.TextureStorage2D(texture, 1, GL.RGBA8, regionAvail[1], regionAvail[2])

        if renderbuffer then
            Memory.write_u32(nativeblock, renderbuffer)
            gl.DeleteRenderbuffers(1, nativeblock)
        end

        gl.CreateRenderbuffers(1, nativeblock)
        renderbuffer = Memory.read_u32(nativeblock)
        gl.NamedRenderbufferStorage(renderbuffer, GL.DEPTH_COMPONENT32, regionAvail[1], regionAvail[2])

        if framebuffer then
            Memory.write_u32(nativeblock, framebuffer)
            gl.DeleteFramebuffers(1, nativeblock)
        end

        gl.CreateFramebuffers(1, nativeblock)
        framebuffer = Memory.read_u32(nativeblock)
        gl.NamedFramebufferTexture(framebuffer, GL.COLOR_ATTACHMENT0, texture, 0)
        gl.NamedFramebufferRenderbuffer(framebuffer, GL.DEPTH_ATTACHMENT, GL.RENDERBUFFER, renderbuffer)
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


        gl.DrawElements(GL.TRIANGLES, count, GL.UNSIGNED_SHORT, Memory.null())
    end

    ImGui.Image(texture, regionAvail, { 0.0, 1.0 }, { 1.0, 0.0 })
end

return {
    OnUnload = function()
        Memory.free(nativeblock)

        if program then
            gl.DeleteProgram(program)
        end

        if texture ~= nil then
            Memory.write_u32(nativeblock, texture)
            gl.DeleteTextures(1, nativeblock)
        end

        if framebuffer ~= nil then
            Memory.write_u32(nativeblock, framebuffer)
            gl.DeleteFramebuffers(1, nativeblock)
        end

        if renderbuffer ~= nil then
            Memory.write_u32(nativeblock, renderbuffer)
            gl.DeleteRenderbuffers(1, nativeblock)
        end

        if vao then
            Memory.write_u32(nativeblock, vao)
            gl.DeleteVertexArrays(1, nativeblock)
        end

        if vbo then
            Memory.write_u32(nativeblock, vbo)
            gl.DeleteBuffers(1, nativeblock)
        end

        if ebo then
            Memory.write_u32(nativeblock, ebo)
            gl.DeleteBuffers(1, nativeblock)
        end
    end,
	gui = {
        title = "Mesh Viewer",
        OnGui = function()
            if not loaded then load() end

            ImGui.Text("%d known meshes", #knownMeshes)
            ImGui.Separator()

            ImGui.Columns(2);

            if ImGui.BeginChild("MeshViewerScrollRegion") then
                for _, value in ipairs(knownMeshes) do
                    if ImGui.Selectable(Aurora.escape(value), selection == value) then
                        errormsg = nil
                        selection = value

                        if vbo then
                            Memory.write_u32(nativeblock, vbo)
                            gl.DeleteBuffers(1, nativeblock)
                        end
                        if vao then
                            Memory.write_u32(nativeblock, vao)
                            gl.DeleteVertexArrays(1, nativeblock)
                        end
                        if ebo then
                            Memory.write_u32(nativeblock, ebo)
                            gl.DeleteBuffers(1, nativeblock)
                        end

                        vao = nil
                        vbo = nil
                        ebo = nil

                        local status, result = pcall(function()
                            local filepath = string.format("%s/cache/%x.pc", Aurora.game_directory(), Aurora.hash(value))
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

                                        local allocation = Memory.malloc(vertexcount * 36)
                                        Memory.write_bytes(allocation, vertexdata, vertexcount * 36)

                                        gl.CreateBuffers(1, nativeblock)
                                        vbo = Memory.read_u32(nativeblock)
                                        gl.NamedBufferStorage(vbo, vertexcount * 36, allocation, GL.NONE)

                                        Memory.free(allocation)

                                        ptr = ptr + vertexcount * 36

                                        -- element list
                                        local trianglecount = read_u32(filebytes, ptr); ptr = ptr + 4
                                        count = trianglecount * 3
                                        -- ptr is now at a location that references the triangle/element list
                                        local elementdata = string.sub(filebytes, ptr)

                                        allocation = Memory.malloc(trianglecount * 6)
                                        Memory.write_bytes(allocation, elementdata, trianglecount * 6)

                                        gl.CreateBuffers(1, nativeblock)
                                        ebo = Memory.read_u32(nativeblock)
                                        gl.NamedBufferStorage(ebo, trianglecount * 6, allocation, GL.NONE)
                                        Memory.free(allocation)

                                        gl.CreateVertexArrays(1, nativeblock)
                                        vao = Memory.read_u32(nativeblock);

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

                    if ImGui.BeginPopupContextItem() then
                        if ImGui.MenuItem("Copy Key") then
                            ImGui.LogToClipboard()
                            ImGui.LogText("%s", Aurora.escape(value))
                            ImGui.LogFinish()
                            ImGui.CloseCurrentPopup()
                        end
                        if ImGui.MenuItem("Copy Hash") then
                            ImGui.LogToClipboard()
                            ImGui.LogText("%x", Aurora.hash(value))
                            ImGui.LogFinish()
                            ImGui.CloseCurrentPopup()
                        end
                        ImGui.EndPopup()
                    end

                    ImGui.SetItemTooltip("Right-click to open context menu");
                end
            end
            ImGui.EndChild()

            ImGui.NextColumn();

            if errormsg then
                ImGui.TextUnformatted(errormsg)
            end

            render()
        end
	},
}