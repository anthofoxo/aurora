package xyz.anthofoxo.aurora.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import xyz.anthofoxo.aurora.UserConfig;
import xyz.anthofoxo.aurora.struct.AuroraReader;
import xyz.anthofoxo.aurora.struct.LibraryImport;
import xyz.anthofoxo.aurora.struct.LibraryObject;
import xyz.anthofoxo.aurora.struct.ObjectDeclaration;
import xyz.anthofoxo.aurora.struct.SequinGate.ParamPath;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;
import xyz.anthofoxo.aurora.struct.experimental.UnknownSkyboxStruct;
import xyz.anthofoxo.aurora.struct.experimental.UnknownSkyboxStruct.Grouping;

public class ObjlibDecomp {
	public ImBoolean visible = new ImBoolean(false);
	private ImString input = new ImString(UserConfig.thumperPath() + "/cache/673863f9.pc", 512);
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
		public int _endskyboxoffset;
		public List<Object> defs = new ArrayList<>();
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

		for (var importObj : level.libraryObjects) {
			if (importObj.type == 0x1BA51443) { // gfx import

				UnknownSkyboxStruct s = new UnknownSkyboxStruct();
				s.header = in.i32arr(2);
				s.unknown0 = in.bool();
				s.groupings = new ArrayList<>();

				int groupCount = in.i32();

				for (int i = 0; i < groupCount; ++i) {
					Grouping g = new Grouping();
					g.params = in.objlist(ParamPath.class);
					g.type = in.str();

					if (g.type.equals("kTraitObj")) {
						g.value = in.str();
					} else if (g.type.equals("kTraitBool")) {
						g.value = in.bool();
					} else throw new IllegalStateException();

					s.groupings.add(g);
				}

				s.comps = in.objlist(Comp.class);

				level.defs.add(s);

			} else throw new IllegalStateException();
		}
		level._endskyboxoffset = in.position();
	}

	private void drawParsed() {
		if (level == null) return;

		ImGui.labelText("content offset", Integer.toHexString(level._startcontentoffset));
		ImGui.labelText("end objlib import", Integer.toHexString(level._endskyboxoffset));
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

			for (int i = 0; i < level.libraryObjects.size(); ++i) {
				var entry = level.libraryObjects.get(i);

				if (ImGui.treeNode(entry.name)) {
					ImGui.labelText("unknown", entry.objlibType + "");
					ImGui.labelText("path", entry.path + "");
					ImGui.labelText("type", entry.type + "");

					ImGui.separator();

					if (level.defs.get(i) instanceof UnknownSkyboxStruct s) {
						ImGui.labelText("unknown", s.unknown0 + "");

						ImGui.labelText("bindings", s.groupings.size() + "");

						ImGui.separator();
						for (int itComp = 0; itComp < s.comps.size(); ++itComp) {
							ImGui.text(s.comps.get(itComp).getClass().getName());
						}

					}

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
