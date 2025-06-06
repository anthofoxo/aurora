# Aurora Documentation
Aurora is an early developement, detailed Thumper decompilation, editor, and exploratory tool. *this is not for casual users*.

Aurora operated directly on the raw Thumper game content with no intermediate formats. This makes is possible the mod and edit Thumper content without converting between tools.

Aurora has a growing collection of tools to assist in reverse enginnering Thumper.

## Tools
### Escaped Inputs
In some situations you may need to hash or lookup a value with non printable ascii. To do this use the `\xHH` escape sequence where `HH` is a hex byte.

For example:
`Atextures/taptap_logo.png\x03\x00\x00\x00`

### Hasher
The hasher is pretty simple, it takes an input, hashes it, tells you the results and checks to see if it matches a cache file.

### Binary Search
Binary search takes the input and scans the entire content of every cache file and provides byte offsets into which files which match the input.
#### Hotkeys
* [Ctrl+C] to copy the output
* [Ctrl+F] to refocus the input box

## Plugin API
The plugin API is **NOT** stable and may break between updates.

See [api.lua](api.lua) for the plugin api reference.

## Changelog
### v0.0.4-a.4
* Extended the hashtable with more values
* Unified plugin api functions
* Added `Aurora.escape` plugin API
* Fixed hashtable inputs from terminating strings early from `NUL` bytes

### v0.0.4-a.3
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

### v0.0.4-a.2
* Updated font files to noto sans
* Hex viewer how has a monospace font
* Linux Support
* Fixed crash when attempting to open hex viewer for `exe` binary search results
* Fixed crash when unpacked exe doesn't exist
* Hashtable support
* Binary hasher will lookup results in the hashtable to display results
* Improved crash handling
* Expanded scripting api with imgui support
* Plugin Support

### v0.0.4-a.1
* Cache binary search
* General purpose hasher
* Hasher informs you when the hash matches a .pc file
* Scripting with exposed hash function