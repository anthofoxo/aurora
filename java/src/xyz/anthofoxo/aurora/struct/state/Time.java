package xyz.anthofoxo.aurora.struct.state;

import xyz.anthofoxo.aurora.struct.ThumperStruct;

public class Time implements ThumperStruct {
	public static final Time Beats = new Time("kTimeBeats");
	public static final Time Seconds = new Time("kTimeSeconds");
	public static final Time BeatsRealtime = new Time("kTimeBeatsRealtime");
	public static final Time SecondsRealtime = new Time("kTimeSecondsRealtime");

	public String str;

	public Time() {
	}

	private Time(String str) {
		this.str = str;
	}
}
