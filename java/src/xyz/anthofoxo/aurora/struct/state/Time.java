package xyz.anthofoxo.aurora.struct.state;

import xyz.anthofoxo.aurora.struct.ThumperStruct;

public enum Time implements ThumperStruct {
	kTimeBeats("kTimeBeats"), kTimeSeconds("kTimeSeconds"), kTimeBeatsRealtime("kTimeBeatsRealtime"), kTimeSecondsRealtime("kTimeSecondsRealtime");
	
	public final String str;
	
	private Time(String str) {
		this.str = str;
	}
}
