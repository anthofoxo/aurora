package xyz.anthofoxo.aurora.tml;

import java.util.ArrayList;
import java.util.List;

import tools.jackson.databind.JsonNode;
import xyz.anthofoxo.aurora.struct.Vec4f;

public class TCLFile {
	public String levelName;
	public String difficulty;
	public String description;
	public String author;
	public float bpm;
	public List<String> sections;
	public Vec4f railsColor;
	public Vec4f railsGlowColor;
	public Vec4f pathColor;
	public Vec4f joyColor;

	public static TCLFile parse(JsonNode node) {
		TCLFile file = new TCLFile();
		file.levelName = node.get("level_name").asString();
		file.difficulty = node.get("difficulty").asString();
		file.description = node.get("description").asString();
		file.author = node.get("author").asString();
		file.bpm = node.get("bpm").asFloat();
		file.sections = new ArrayList<>();
		for (var n : node.get("level_sections")) file.sections.add(n.asString());
		file.railsColor = Vec4f.parse(node.get("rails_color"));
		file.railsGlowColor = Vec4f.parse(node.get("rails_glow_color"));
		file.pathColor = Vec4f.parse(node.get("path_color"));
		file.joyColor = Vec4f.parse(node.get("joy_color"));
		return file;
	}
}
