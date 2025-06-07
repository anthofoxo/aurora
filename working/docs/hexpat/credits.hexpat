struct CreditSection {
	sstr header;
	u32 count;
	sstr items[count];
};

struct CreditHeading {
	sstr decoration;
	sstr text;
};

struct CreditHeadingGroup {
	u32 count;
	CreditHeading items[count];
};

struct Credits {
	u32 header;
	
	u32 headingCount;
	CreditHeadingGroup headings[headingCount];
	
	u32 groupCount;
	CreditSection items[groupCount];
};