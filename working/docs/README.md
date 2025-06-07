# Aurora Documentation
Aurora is an early development, detailed Thumper decompilation, editor, and exploratory tool. *this is not for casual users*.

Aurora operated directly on the raw Thumper game content with no intermediate formats. This makes it possible the mod and edit Thumper content without converting between tools.

Aurora has a growing collection of tools to assist in reverse engineering Thumper.

View [changelogs](changelog.md).

## Tools
### Escaped Inputs
In some situations you may need to hash or lookup a value with non-printable ascii. To do this use the `\xHH` escape sequence where `HH` is a hex byte.

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