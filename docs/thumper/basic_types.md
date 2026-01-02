# Overview

## Signed vs Unsigned
Some types are signed meaning they can support negative numbers. Unsigned numbers do not support negative numbers. Signed types start with an `s`, unsigned types start with a `u`.

## Endianess
All datatypes are stored in little endian byte order.

## Basic Data Types

### u8 / s8
A 8 bit integer.

### u16 / s16
A 16 bit integer.

### u32 / s32
A 32 bit integer.

### u64 / s64
A 64 bit integer.

## Aliases

## bool
An alias for `u8`. The only valid values are `0`/`false` and `1`/`true`.

## char
An alias for `u8` or `s8`. This is used to represent strings.

## time
An alias for `u64`. This is the native c type `time64_t`. This stores the number of seconds since the unix epoch. This value is not offset into local timezones.

## Floating Points

## float / double
Floats are a 32 bit floating point value. Double are 64 bit floating point values.

These values adhere to IEEE 754.

## Arrays
Array types can be bound or unbound. A bound array means we know how large this array is expected to be. An unbound array means we do not know this. More information would need collected. Arrays can also use other fields for their array length. An example of some arrays are below.

* `u8[8]` Exactly 8 bytes
* `u32[..2]` 0 to 2 `u32`
* `u32[2..3]` 2 or 3 `u32`
* `u32[2..]` 2 or more `u32`
* `u32[...]` Completely unknown array size
* `u8[count]` Array is `count` bytes long

## Composite Types

### sstr
Short for "Sized String". Most string used in thumper are sized strings. If `str` is seen. Assume this is a `sstr`.

Strings are simply a count value followed by that number of bytes.

```
struct sstr {
    u32 count;
    char bytes[count];
};
```

### cstr
Short for C-Style string. C style string do not have a size and rather they start scanning chars until a null(`0`) byte is encountered.

```
struct cstr {
    char bytes[while($ != 0x00)];
};
```

### Vec2f
```
struct vec2f {
    f32 v[2];
};
```

### Vec3f
```
struct vec3f {
    f32 v[3];
};
```

### Vec4f
```
struct vec4f {
    f32 v[4];
};
```

### Transform
Transform objects are seen in some cases. Resembles a matrix.

```
struct Transform {
    Vec3f translation;
    Vec3f basisX;
    Vec3f basisY;
    Vec3f basisZ;
    Vec3f scale;
};
```

## Generic Types
Some types can assume what datatypes expect to be filled at a later time. Datapoints are the main example of this.

### DataPoint
Datapoints are used to create sequencers, usually youll see these with `T = bool` or `T = f32`. However other types are allowed and are seen used.

```
struct DataPoint<T> {
    f32 time;
    T value;
    sstr interp;
    sstr easing;
}
```