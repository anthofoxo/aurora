package xyz.anthofoxo.aurora.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.UserConfig;
import xyz.anthofoxo.aurora.struct.AuroraReader;
import xyz.anthofoxo.aurora.struct.EntitySpawner;
import xyz.anthofoxo.aurora.struct.LibraryImport;
import xyz.anthofoxo.aurora.struct.LibraryObject;
import xyz.anthofoxo.aurora.struct.ObjectDeclaration;
import xyz.anthofoxo.aurora.struct.Sample;
import xyz.anthofoxo.aurora.struct.SequinDrawer;
import xyz.anthofoxo.aurora.struct.SequinGate;
import xyz.anthofoxo.aurora.struct.SequinGate.ParamPath;
import xyz.anthofoxo.aurora.struct.SequinLeaf;
import xyz.anthofoxo.aurora.struct.SequinLevel;
import xyz.anthofoxo.aurora.struct.SequinMaster;
import xyz.anthofoxo.aurora.struct.Tex2D;
import xyz.anthofoxo.aurora.struct.TraitAnim;
import xyz.anthofoxo.aurora.struct.Xfmer;
import xyz.anthofoxo.aurora.struct._Mesh;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;
import xyz.anthofoxo.aurora.struct.comp.DrawComp;
import xyz.anthofoxo.aurora.struct.experimental.UnknownSkyboxStruct;
import xyz.anthofoxo.aurora.struct.experimental.UnknownSkyboxStruct.Grouping;

public class ObjlibDecomp {
	public ImBoolean visible = new ImBoolean(false);
	// C:\Program Files (x86)\Steam\steamapps\common\Thumper/cache/b2455736.pc
	private ImString input = new ImString(UserConfig.thumperPath() + "/cache/b2455736.pc", 512);
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

		public List<Sample> samples = new ArrayList<>();
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

				// when parsing a skybox, drawcomp needs one field stripped, ensure the parser
				// knows this dependency
				in.enclosing.push(UnknownSkyboxStruct.class);
				s.comps = in.objlist(Comp.class);
				in.enclosing.pop();

				level.defs.add(s);

			} else throw new IllegalStateException();
		}
		level._endskyboxoffset = in.position();

		quit_reading: for (var declaration : level.objectDeclarations) {
			switch (declaration.type) {
			case SequinLeaf:
				level.defs.add(in.obj(SequinLeaf.class));
				break;
			case SequinMaster:
				level.defs.add(in.obj(SequinMaster.class));
				break;
			case SequinDrawer:
				level.defs.add(in.obj(SequinDrawer.class));
				break;
			case SequinLevel:
				level.defs.add(in.obj(SequinLevel.class));
				break;
			case Sample:
				level.defs.add(in.obj(Sample.class));
				break;
			case EntitySpawner:
				level.defs.add(in.obj(EntitySpawner.class));
				break;
			case SequinGate:
				level.defs.add(in.obj(SequinGate.class));
				break;
			case Tex2D:
				level.defs.add(in.obj(Tex2D.class));
				break;
			case Xfmer:
				level.defs.add(in.obj(Xfmer.class));
				break;
			case Mesh:
				level.defs.add(in.obj(_Mesh.class));
				break;
			case TraitAnim:
				level.defs.add(in.obj(TraitAnim.class));
				break;
				
			case Path:
				level.defs.add(in.obj(xyz.anthofoxo.aurora.struct.Path.class));
				break;
			default:
				System.err.println("We dont know how to read: " + declaration.name + " at offset "
						+ Integer.toHexString(in.position()) + "; further reading is cancelled");
				break quit_reading;
			}
		}

		System.out.println();
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
							var comp = s.comps.get(itComp);

							ImGui.text(s.comps.get(itComp).getClass().getName());

							if (comp instanceof DrawComp c) {
								ImGui.text(c.bucket.toString());
								ImGui.text(c.layer.toString());
							}

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

			String prefix = UserConfig.thumperPath() + "/cache/";

			if (ImGui.button("Title Screen")) {
				input.set(prefix + Integer.toHexString(Hash.fnv1a("Alevels/title_screen.objlib")) + ".pc");
			}
			ImGui.sameLine();
			if (ImGui.button("Level 1")) {
				input.set(prefix + Integer.toHexString(Hash.fnv1a("Alevels/demo.objlib")) + ".pc");
			}
			ImGui.sameLine();
			if (ImGui.button("Level 2")) {
				input.set(prefix + Integer.toHexString(Hash.fnv1a("Alevels/level2/level_2a.objlib")) + ".pc");
			}
			ImGui.sameLine();
			if (ImGui.button("Level 3")) {
				input.set(prefix + Integer.toHexString(Hash.fnv1a("Alevels/level3/level_3a.objlib")) + ".pc");
			}
			ImGui.sameLine();
			if (ImGui.button("Level 4")) {
				input.set(prefix + Integer.toHexString(Hash.fnv1a("Alevels/level4/level_4a.objlib")) + ".pc");
			}
			ImGui.sameLine();
			if (ImGui.button("Level 5")) {
				input.set(prefix + Integer.toHexString(Hash.fnv1a("Alevels/level5/level_5a.objlib")) + ".pc");
			}
			ImGui.sameLine();
			if (ImGui.button("Level 6")) {
				input.set(prefix + Integer.toHexString(Hash.fnv1a("Alevels/level6/level_6.objlib")) + ".pc");
			}
			ImGui.sameLine();
			if (ImGui.button("Level 7")) {
				input.set(prefix + Integer.toHexString(Hash.fnv1a("Alevels/level7/level_7a.objlib")) + ".pc");
			}
			ImGui.sameLine();
			if (ImGui.button("Level 8")) {
				input.set(prefix + Integer.toHexString(Hash.fnv1a("Alevels/level8/level_8a.objlib")) + ".pc");
			}
			ImGui.sameLine();
			if (ImGui.button("Level 9")) {
				input.set(prefix + Integer.toHexString(Hash.fnv1a("Alevels/level9/level_9a.objlib")) + ".pc");
			}

			ImGui.separator();

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
