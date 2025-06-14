
# Changelogs
## v0.0.4-a.5
* Plugin IDs are created using the filename stem
* Expanded hashtable entries
* Many OpenGL APIs exposed to allow custom rendering (Targeting 4.5 Core)
* Fixed uncaught errors during `Plugin.Unload` causing program crashes
* Plugins how have "persistent storage" meaning certain values are handled in native code and will persist between reloads
* `Plugin.visible` is now moved to persistent storage
* Reloading plugins will remember the visible windows

### Breaking Changes
* Filesystem APIs are now in the `Aurora.filesystem` table instead of the `Aurora` table
* `Plugin.visible` changed type from `boolean` to `boolean?`
* `ImGui.MenuItem` parameter count reduced from 2 to 1

#### Moved API Functions
* `Aurora.is_regular_file` -> `Aurora.filesystem.is_regular_file`
* `Aurora.directory_iterator` -> `Aurora.filesystem.directory_iterator`
* `Aurora.create_directory` -> `Aurora.filesystem.create_directory`
* `Aurora.create_directories` -> `Aurora.filesystem.create_directories`

#### New API Functions
* `Aurora.filesystem.stem`
* `Aurora.util.create_shader_program`
* `Aurora.bitcast_float`
* `ImGui.LogToClipboard`
* `ImGui.LogFinish`
* `ImGui.LogText`
* `ImGui.SmallButton`
* `ImGui.GetContentRegionAvail`
* `ImGui.BeginPopupContextItem`
* `ImGui.EndPopup`
* `ImGui.CloseCurrentPopup`
* `ImGui.SetItemTooltip`
* `glm.perspective`
* `glm.lookAt`
* `gl.AttachShader`
* `gl.BindFramebuffer`
* `gl.BindVertexArray`
* `gl.ClearNamedFramebufferfv`
* `gl.CompileShader`
* `gl.CreateFramebuffers`
* `gl.CreateProgram`
* `gl.CreateRenderbuffers`
* `gl.CreateShader`
* `gl.CreateVertexArrays`
* `gl.DeleteFramebuffers`
* `gl.DeleteProgram`
* `gl.DeleteRenderbuffers`
* `gl.DeleteShader`
* `gl.DeleteVertexArrays`
* `gl.DetachShader`
* `gl.DrawArrays`
* `gl.DrawElements`
* `gl.EnableVertexArrayAttrib`
* `gl.GetUniformLocation`
* `gl.LinkProgram`
* `gl.NamedFramebufferRenderbuffer`
* `gl.NamedFramebufferTexture`
* `gl.NamedRenderbufferStorage`
* `gl.ProgramUniformMatrix4fv`
* `gl.ShaderSource`
* `gl.UseProgram`
* `gl.VertexArrayAttribBinding`
* `gl.VertexArrayAttribFormat`
* `gl.VertexArrayElementBuffer`
* `gl.VertexArrayVertexBuffer`
* `gl.Viewport`

## v0.0.4-a.4
* Extended the hashtable with more values
* Unified plugin api functions
* Fixed hashtable inputs from terminating strings early from `NUL` bytes
* Hasher will now directly lookup the input in the hashtable. e.g. `673863f9 -> Alevels/demo.objlib`
* Improved error and crash handling of scripts
* All plugins can be reloaded without restarting Aurora from the `Plugins > Reload Plugins` option
* Some plugin internals have been moved into native code
* Better Plugin API Documentation
* Plugin windows now have a default size
* Hashtable and plugin processes are now joined together
* Improve native error handing
* Fixed sign extension bug with `Aurora.escape`
* New fuzzy hashtable search
* Removed `boot.lua:OnUpdate` function. Now handled in native code.
* Fixed bug preventing the console from being closed
* Started exposing OpenGL API functions
* Added plugin unload hook
* Added texture viewer and exporter

### New API Functions
* `Aurora.escape`
* `Aurora.read_file`
* `Aurora.write_file`
* `Aurora.create_directory`
* `Aurora.create_directories`
* `Aurora.ddsktx_parse`
* `ImGui.LabelText`
* `ImGui.Separator`
* `ImGui.SameLine`
* `ImGui.TextColored`
* `ImGui.PushStyleVar`
* `ImGui.PopStyleVar`
* `ImGui.Selectable`
* `ImGui.Image`
* `ImGui.BeginTable`
* `ImGui.EndTable`
* `ImGui.TableNextRow`
* `ImGui.TableSetColumnIndex`
* `ImGui.Columns`
* `ImGui.NextColumn`
* `ImGui.BeginChild`
* `ImGui.EndChild`
* `gl.CreateVertexArrays`
* `gl.NamedBufferStorage`
* `gl.CreateBuffers`
* `gl.DeleteBuffers`
* `gl.CreateTextures`
* `gl.DeleteTextures`

### Removed API Functions
* `ImGui.Begin`
* `ImGui.End`
* `ImGui.BeginMainMenuBar`
* `ImGui.EndMainMenuBar`

## v0.0.4-a.3
* Extended the hashtable with more values
* Fixed allocation bug with non-ascii hashtable values
* Created basic documentation
* Empty binary lookup inputs no longer break the tool
* Removed changelog plugin
* Removed help menus
* Added `Aurora.unescape` plugin API
* Added `Aurora.game_directory` plugin API
* Added `ImGui.Button` plugin API
* Hasher plugin now has feature parity with the builtin
* Removed builtin hasher

## v0.0.4-a.2
* Updated font files to noto sans
* Hex viewer how has a monospace font
* Linux Support
* Fixed crash when attempting to open hex viewer for `exe` binary search results
* Fixed crash when unpacked exe doesn't exist
* Hashtable support
* Binary hasher will look up results in the hashtable to display results
* Improved crash handling
* Expanded scripting api with imgui support
* Plugin Support

## v0.0.4-a.1
* Cache binary search
* General purpose hasher
* Hasher informs you when the hash matches a .pc file
* Scripting with exposed hash function