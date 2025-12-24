package xyz.anthofoxo.aurora.struct;

import java.util.ArrayList;
import java.util.List;

import xyz.anthofoxo.aurora.tml.TCLFile;

public class SectionFile implements ThumperStruct {

	public int fileType = 9;
	public List<String> sections;
	public Vec4f railsColor;
	public Vec4f railsGlowColor;
	public Vec4f pathColor;
	public Vec4f joyColor;

	public static SectionFile fromTML(TCLFile obj) {
		SectionFile file = new SectionFile();
		file.sections = new ArrayList<String>();

		for (var entry : obj.sections) {
			file.sections.add(entry);
		}

		file.railsColor = obj.railsColor;
		file.railsGlowColor = obj.railsGlowColor;
		file.pathColor = obj.pathColor;
		file.joyColor = obj.joyColor;
		return file;
	}
}
