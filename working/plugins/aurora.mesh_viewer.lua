local knownMeshes = {}
local selection = nil
local contentRegionAvailLast = { 0, 0 }
local texture = 0
local framebuffer = 0
local renderbuffer = 0

for _, value in pairs(Aurora.hashtable()) do
    if string.match(value, ".x") then
        table.insert(knownMeshes, value)
    end
end

return {
    OnUnload = function()
        if texture ~= 0 then
            gl.DeleteTextures(texture)
        end

        if framebuffer ~= 0 then
            gl.DeleteFramebuffers(framebuffer)
        end

        if renderbuffer ~= 0 then
            gl.DeleteRenderbuffers(renderbuffer)
        end
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

                if texture ~= 0 then
                    gl.DeleteTextures(texture)
                end

                texture = gl.CreateTextures(GL.TEXTURE_2D)
                gl.TextureStorage2D(texture, 1, GL.RGBA8, regionAvail[1], regionAvail[2])

                if renderbuffer ~= 0 then
                    gl.DeleteRenderbuffers(renderbuffer)
                end

                renderbuffer = gl.CreateRenderbuffers()
                gl.NamedRenderbufferStorage(renderbuffer, GL.DEPTH_COMPONENT32, regionAvail[1], regionAvail[2])

                if framebuffer ~= 0 then
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
            gl.BindFramebuffer(GL.FRAMEBUFFER, 0)

            ImGui.Image(texture, regionAvail)
        end
	},
}