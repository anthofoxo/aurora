package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;

public class PostProcess {
	public static int[] header() {
		return new int[] { 0x02, 0x04 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public List<Comp> comps;
	
	// editstatecomp
	// A4 C2 F8 31
	// int 4
	
	// 0.03
	// 0.7
	//1
	//1
	//1
	//1
	//0
	//0.2
	//0.5
	//1
	//0.86
	//0.86
	//0.86
	// 1
	// bool 1
	// unknown A4 C2 F8 31
	
}
