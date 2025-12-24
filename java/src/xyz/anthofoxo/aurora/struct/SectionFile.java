package xyz.anthofoxo.aurora.struct;

import java.util.ArrayList;
import java.util.List;

import tools.jackson.databind.JsonNode;

public class SectionFile implements ThumperStruct {
	public static class Color implements ThumperStruct {
		public float r, g, b, a;

		public Color() {
		}

		public Color(float r, float g, float b, float a) {
			this.r = r;
			this.g = g;
			this.b = b;
			this.a = a;
		}
	}

	public int fileType = 9;
	public List<String> sections;
	public Color railsColor;
	public Color railsGlowColor;
	public Color pathColor;
	public Color joyColor;

	private static Color parseColor(JsonNode obj) {
		return new Color(obj.get(0).asFloat(), obj.get(1).asFloat(), obj.get(2).asFloat(), obj.get(3).asFloat());
	}

	public static SectionFile fromTML(JsonNode obj) {
		SectionFile file = new SectionFile();
		file.sections = new ArrayList<String>();

		for (var entry : obj.get("level_sections")) {
			file.sections.add(entry.asString());
		}

		file.railsColor = parseColor(obj.get("rails_color"));
		file.railsGlowColor = parseColor(obj.get("rails_glow_color"));
		file.pathColor = parseColor(obj.get("path_color"));
		file.joyColor = parseColor(obj.get("joy_color"));
		return file;
	}
}
