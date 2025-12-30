# Overall Structure of Game Files
Most of the game files, including 3D meshes, textures, audio samples and game object libraries are located in the `cache` directory in the game's installation files. All of them are cryptically named with hex characters plus a `.pc` file extension. The cryptic file names are resulted from applying a 32-bit hash function to their original file paths. For game object libraries and audio samples, their original file paths or file names are stored within the game files themselves and can be extracted.

## File Types
The first 4 bytes of each game file is an `u32` identifying its file type. All possible values are listed in the table below.

|Value|Meaning|
|-----|-------|
|`0`  |Scoring file. Defines the scoring fules, such as how many points are awarded for each type of action and no miss and no damage bonuses.|
|`4`  |Configuration files.|
|`5`  |A list of UI menus, such as the credits, level select, leaderboards, options, and in-game pause screen.|
|`6`  |Meshes. These are stored in a proprietary format.|
|`8`  |Game object library. These are a custom format.|
|`9`  |Credit files and level configuration files.|
|`12` |Textures. Stored as `.dds`.|
|`13` |Audio sample, These are stores as `FSB5 MFOD Banks`
|`14` |Level listing.|
|`28` |Shaders.|
|`93` |Unknown.|