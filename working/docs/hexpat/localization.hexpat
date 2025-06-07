struct LocalizationEntry {
	u32 key;    // Hashed translation key value e.g. `continue`
	u32 offset; // Offsets into `cstr values[]`
};

struct Localization {
	u32 header;
	u32 numKeys;
	u32 numBytes;
	cstr values[numKeys];
	LocalizationEntry enteries[numKeys];
};