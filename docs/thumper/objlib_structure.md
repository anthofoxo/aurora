# General Structure of `.objlib` Files
Game object library (`.objlib`) files are game files in a custom-made format, created and edited in Drool's Editor. They are arguably the most important type of game files, defining and controlling various interactive and decorative visual elements and audio elements in the game.

In each `.objlib` file, after the first 4 bytes containing the file type identifier, the next 4 bytes contain a "magic number" identifying what type of `.objlib` file it is. The following table lists all possible values and their meanings.

|Value BE    |Value LE    |Meaning|Example|
|------------|------------|-------|-------|
|`0x4314a51b`|`0x1ba51443`|GFX Library. Most `.objlib` files that do not fit into the other types are in this category, including `.objlib` files that contain UI objects, bosses, visual effects, skyboxes, interactive visual objects with simple animations.|`vr_toggle.objlib` (`c71afe7f.pc`)
|`0x484595b0`|`0xb0954548`|Sequin library. These are `.objlib` files that contain decorative visual objects with complex animations that require more sequencing control.|`ducker_ring_sequin.objlib` (`69e6e7e2.pc`)|
|`0x19621c9d`|`0x9d1c6219`|Obj library. These are `.objlib` files that control the audio aspect of the game, including channels and SFX, and VR playspace scale.|`vr_playspace.objlib` (`ae6469be.pc`)|
|`0x9e4d370b`|`0x0b374d9e`|Level library. These are `.objlib` files that control the sequence and timing of objects appearing in a level.|`demo.objlib` (`673863f9.pc`)|
|`0x4f6274e6`|`0xe674624f`|Avatar library. This is the `.objlib` file that control the appearance of the beetle and its reactions to input and obstacles.|`avatar.objlib` (`cd2c1ec.pc`)|

## File Format
Each `.objlib` is divided into 6 main sections.

### Header
This section contains the file type identifier `8`, which stands for `.objlib` and the specific type of `.objlib` filee. It also contains a few `u32` values of which the purpose are unclear at the moment. These values are probably not important in the grand sceme of things, as all `.objlib` files of the same type seem to contain the same set of values. The values that are included are slightly different between different types of `.objlib` files.

## Global Library List
The section lists the global `.objlib` files referenced but the current `.objlib` file. Objects defined in the `Object Definition` section can reference any of the objects contained in these global `.objlib` files. These global `.objlib` files can contain more global `.objlib` files, whose objects can be referenced by the current `.objlib` file as well.

## File Information
This section contains the original file path to the current `.objlib` file.

## Object List
This section lists the current external objects reference by the current `.objlib` files, and the objects defined within the current `.objlib` file. For each external object, the `object type`, object name and the original file path of the `.objlib` file containing that object are specified, For each object defined within the current file, the `object type` amd pbkect name are specified.

## Object Definition
This section contains the definition of each object listed in the `Object List` section. The objects in this section are defined in the same order as they are listed in the `Object List` section. There is no clear marker to separate the boundary between two objects.

For objects that have an empty main definition section, such as `.xfm`/`.xfmer` objects. This content is omitted.

Many objects include a Component list near the start of its definition body. Making it a little easier to separate objects out.

## Footer
This section contains data and object related to the `.objlib` file itself. The specific types of data and objects included depend on the type of `.objlib` file.

## Struct Overview
```hexpat
struct sstr {
    u32 numBytes;
    char bytes[numBytes];
};

struct ObjlibReference {
    u32;
    sstr path;
};

struct ExternalObject {
    u32 type;
    sstr name;
    u32 objlibType;
    sstr objlibPath;
};

struct ObjectDeclaration {
    u32 type;
    sstr name;
};

struct ObjlibFile {
    u32 fileType; // 8
    u32 objlibType;
    u8[...]; // The rest of the header bytes

    // This is the Global Library List
    u32 objlibReferenceCount;
    ObjlibReference[objlibReferenceCount];

    // File information
    stsr objlibPath;

    // External References
    u32 externalObjectCount;
    ExternalObject[externalObjectCount];

    // Objects Declarations
    u32 objectDeclarationCount;
    ObjectDeclaration[objectDeclarationCount];

    // External Reference Definitions
    // This is typically used in levels to attach objects to specific bind points of a skybox.
    // Each structure in this list depends on what was declared above.
    Any[externalObjectCount];

    // Object Definitions
    // This contains all the objects defined by the objlib
    // In the same order as declared above
    Any[objectDeclarationCount];

    // Footer, the remaining data of the .objlib
    u8[...];
};
```