package xyz.anthofoxo.aurora.struct.trait;

import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.parse.AuroraWriter;
import xyz.anthofoxo.aurora.struct.ThumperStruct;
import xyz.anthofoxo.aurora.struct.annotation.UnknownNativeName;

@UnknownNativeName
public enum Time implements ThumperStruct {
	kTimeBeats, kTimeSeconds, kTimeBeatsRealtime, kTimeSecondsRealtime;

	public static Time in(AuroraReader in) {
		return valueOf(in.str());
	}

	public static void out(AuroraWriter out, Time v) {
		out.str(v.name());
	}
}
