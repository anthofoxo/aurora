package xyz.anthofoxo.aurora.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import xyz.anthofoxo.aurora.UserConfig;
import xyz.anthofoxo.aurora.struct.AuroraReader;
import xyz.anthofoxo.aurora.struct.FixedSize;
import xyz.anthofoxo.aurora.struct.LibraryImport;
import xyz.anthofoxo.aurora.struct.LibraryObject;
import xyz.anthofoxo.aurora.struct.ObjectDeclaration;

public class ObjlibDecomp {
	public ImBoolean visible = new ImBoolean(false);
	private ImString input = new ImString(
			"C:\\Program Files (x86)\\Steam\\steamapps\\common\\Thumper\\cache\\673863f9.pc", 512);
	private String error = "";

	public static class ObjlibLevel {
		public int fileType;
		public int objlibType;
		@FixedSize(count = 4)
		public int[] unknownHeader;
		public List<LibraryImport> libraryImports;
		public String levelPath;
		public List<LibraryObject> libraryObjects;
		public List<ObjectDeclaration> objectDeclarations;
		public int _startcontentoffset;
	}

	private ObjlibLevel level = null;

	private void parse() throws IOException {
		byte[] bytes = null;
		level = null;

		if (Files.exists(Path.of(input.get()))) bytes = Files.readAllBytes(Path.of(input.get()));

		if (bytes == null) {
			error += "Cannot read file\n";
			return;
		}

		AuroraReader in = new AuroraReader(bytes);
		level = new ObjlibLevel();
		level.fileType = in.i32();
		level.objlibType = in.i32();
		level.unknownHeader = in.i32arr(4);
		level.libraryImports = in.objlist(LibraryImport.class);
		level.levelPath = in.str();
		level.libraryObjects = in.objlist(LibraryObject.class);
		level.objectDeclarations = in.objlist(ObjectDeclaration.class);
		level._startcontentoffset = in.pos;
	}

	private void drawParsed() {
		if (level == null) return;

		ImGui.labelText("content offset", level._startcontentoffset + "");
		ImGui.separator();
		ImGui.labelText("path", level.levelPath);

		if (ImGui.collapsingHeader("Library Imports")) {
			for (var entry : level.libraryImports) {
				if (ImGui.treeNode(entry.path)) {
					ImGui.labelText("unknown", entry.unknown + "");
					ImGui.treePop();
				}
			}
		}

		if (ImGui.collapsingHeader("Library Declarations")) {
			for (var entry : level.libraryObjects) {
				if (ImGui.treeNode(entry.name)) {
					ImGui.labelText("unknown", entry.unknown + "");
					ImGui.labelText("path", entry.path + "");
					ImGui.labelText("type", entry.type + "");
					ImGui.treePop();
				}
			}
		}

		if (ImGui.collapsingHeader("Object Declarations")) {
			for (var entry : level.objectDeclarations) {
				if (ImGui.treeNode(entry.name)) {
					ImGui.labelText("type", entry.type + "");
					ImGui.treePop();
				}
			}
		}

	}

	public void draw() {
		if (!visible.get()) return;

		if (Hasher.commExport != null) {
			input.set(UserConfig.thumperPath() + "/cache/" + Hasher.commExport + ".pc");
			Hasher.commExport = null;
		}

		if (ImGui.begin("Objlib Decomp", visible)) {

			ImGui.inputText("Open Objlib", input);
			ImGui.sameLine();
			if (ImGui.button("Parse")) {
				error = "";
				try {
					parse();
				} catch (IOException e) {
					error += e.toString();
				}
			}

			if (!error.isEmpty()) {
				ImGui.text(error);
			} else {
				drawParsed();
			}

		}
		ImGui.end();
	}
}
