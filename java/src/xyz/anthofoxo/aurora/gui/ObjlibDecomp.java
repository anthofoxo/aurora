package xyz.anthofoxo.aurora.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import xyz.anthofoxo.aurora.Hash;
import xyz.anthofoxo.aurora.UserConfig;
import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.struct.Bender;
import xyz.anthofoxo.aurora.struct.Cam;
import xyz.anthofoxo.aurora.struct.ChannelGroup;
import xyz.anthofoxo.aurora.struct.DSP;
import xyz.anthofoxo.aurora.struct.DSPChain;
import xyz.anthofoxo.aurora.struct.DrawGroup;
import xyz.anthofoxo.aurora.struct.EntitySpawner;
import xyz.anthofoxo.aurora.struct.Env;
import xyz.anthofoxo.aurora.struct.Flow;
import xyz.anthofoxo.aurora.struct.GfxLibImport;
import xyz.anthofoxo.aurora.struct.GfxLibImport.Grouping;
import xyz.anthofoxo.aurora.struct.LevelLibFooter;
import xyz.anthofoxo.aurora.struct.LibraryObject;
import xyz.anthofoxo.aurora.struct.LibraryType;
import xyz.anthofoxo.aurora.struct.Mat;
import xyz.anthofoxo.aurora.struct.Mesh;
import xyz.anthofoxo.aurora.struct.ObjlibFooter;
import xyz.anthofoxo.aurora.struct.PathDecorator;
import xyz.anthofoxo.aurora.struct.PostProcess;
import xyz.anthofoxo.aurora.struct.PostProcessPass;
import xyz.anthofoxo.aurora.struct.Sample;
import xyz.anthofoxo.aurora.struct.Scene;
import xyz.anthofoxo.aurora.struct.SequinDrawer;
import xyz.anthofoxo.aurora.struct.SequinGate;
import xyz.anthofoxo.aurora.struct.SequinLeaf;
import xyz.anthofoxo.aurora.struct.SequinLevel;
import xyz.anthofoxo.aurora.struct.SequinMaster;
import xyz.anthofoxo.aurora.struct.SequinPulse;
import xyz.anthofoxo.aurora.struct.Tex2D;
import xyz.anthofoxo.aurora.struct.TraitAnim;
import xyz.anthofoxo.aurora.struct.Vibration;
import xyz.anthofoxo.aurora.struct.VrSettings;
import xyz.anthofoxo.aurora.struct.Xfmer;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;
import xyz.anthofoxo.aurora.struct.objlib.DeclarationType;
import xyz.anthofoxo.aurora.struct.objlib.FileType;
import xyz.anthofoxo.aurora.struct.objlib.LibraryImport;
import xyz.anthofoxo.aurora.struct.objlib.ObjectDeclaration;
import xyz.anthofoxo.aurora.struct.sequin.ParamPath;

public class ObjlibDecomp {
	public ImBoolean visible = new ImBoolean(false);
	private ImString input = new ImString("", 512);
	private String error = "";

	public static class ObjlibLevel {
		public FileType fileType;
		public LibraryType libraryType;
		@FixedSize(count = 4)
		public int[] unknownHeader;
		public List<LibraryImport> libraryImports;
		public String levelPath;
		public List<LibraryObject> libraryObjects;
		public List<ObjectDeclaration> objectDeclarations;
		public int _startcontentoffset;
		public int _endskyboxoffset;

		public List<GfxLibImport> gfxImports = new ArrayList<>();

		public Map<String, xyz.anthofoxo.aurora.struct.Path> paths = new HashMap<>();
		public Map<String, SequinLeaf> leafs = new HashMap<>();
		public Map<String, SequinMaster> masters = new HashMap<>();
		public Map<String, SequinLevel> levels = new HashMap<>();
		public Map<String, SequinGate> gates = new HashMap<>();
		public Map<String, Cam> cams = new HashMap<>();
		public Map<String, Bender> benders = new HashMap<>();
		public Map<String, Env> envs = new HashMap<>();
		public Map<String, SequinDrawer> drawers = new HashMap<>();
		public Map<String, Sample> samples = new HashMap<>();
		public Map<String, TraitAnim> anims = new HashMap<>();
		public Map<String, Mesh> meshes = new HashMap<>();
		public Map<String, VrSettings> vrsettings = new HashMap<>();
		public Map<String, EntitySpawner> spawners = new HashMap<>();
		public Map<String, Tex2D> tex2ds = new HashMap<>();
		public Map<String, Xfmer> xfmers = new HashMap<>();
		public Map<String, ChannelGroup> channelGroups = new HashMap<>();
		public Map<String, DSPChain> dchs = new HashMap<>();
		public Map<String, Vibration> vibs = new HashMap<>();
		public Map<String, PathDecorator> decorators = new HashMap<>();
		public Map<String, Scene> scenes = new HashMap<>();
		public Map<String, PostProcess> postProcess = new HashMap<>();
		public Map<String, PostProcessPass> passes = new HashMap<>();
		public Map<String, SequinPulse> pulses = new HashMap<>();
		public Map<String, Mat> mats = new HashMap<>();
		public Map<String, DSP> dsps = new HashMap<>();
		public Map<String, DrawGroup> drawGroups = new HashMap<>();

		public ObjlibFooter genericFooter;
		public Object libraryFooter;
	}

	private ObjlibLevel level = null;

	private static Map<DeclarationType, int[]> objectHeaders = new HashMap<>();

	static {
		objectHeaders.put(DeclarationType.SequinLeaf, SequinLeaf.header());
		objectHeaders.put(DeclarationType.SequinMaster, SequinMaster.header());
		objectHeaders.put(DeclarationType.Vibration, Vibration.header());
		objectHeaders.put(DeclarationType.SequinDrawer, SequinDrawer.header());
		objectHeaders.put(DeclarationType.SequinLevel, SequinLevel.header());
		objectHeaders.put(DeclarationType.Sample, Sample.header());
		objectHeaders.put(DeclarationType.EntitySpawner, EntitySpawner.header());
		objectHeaders.put(DeclarationType.SequinGate, SequinGate.header());
		objectHeaders.put(DeclarationType.Tex2D, Tex2D.header());
		objectHeaders.put(DeclarationType.Xfmer, Xfmer.header());
		objectHeaders.put(DeclarationType.Cam, Cam.header());
		objectHeaders.put(DeclarationType.Mesh, Mesh.header());
		objectHeaders.put(DeclarationType.TraitAnim, TraitAnim.header());
		objectHeaders.put(DeclarationType.PathDecorator, PathDecorator.header());
		objectHeaders.put(DeclarationType.DSPChain, DSPChain.header());
		objectHeaders.put(DeclarationType.Path, xyz.anthofoxo.aurora.struct.Path.header());
		objectHeaders.put(DeclarationType.SequinPulse, SequinPulse.header());
		objectHeaders.put(DeclarationType.Mat, Mat.header());
		objectHeaders.put(DeclarationType.ChannelGroup, ChannelGroup.header());
		objectHeaders.put(DeclarationType.Flow, Flow.header());
		objectHeaders.put(DeclarationType.Scene, Scene.header());
		objectHeaders.put(DeclarationType.PostProcess, PostProcess.header());
		objectHeaders.put(DeclarationType.PostProcessPass, PostProcessPass.header());
		objectHeaders.put(DeclarationType.Bender, Bender.header());
		objectHeaders.put(DeclarationType.DrawGroup, DrawGroup.header());
	}

	private void parse() throws IOException {
		byte[] bytes = null;
		level = null;

		if (Files.exists(Path.of(input.get()))) bytes = Files.readAllBytes(Path.of(input.get()));

		if (bytes == null) {
			error += "Cannot read file\n";
			return;
		}

		System.out.println("========================\n========================\n==================="
				+ "\nStaring parse of " + input.get());

		AuroraReader in = new AuroraReader(bytes);
		level = new ObjlibLevel();
		level.fileType = in.obj(FileType.class);
		level.libraryType = in.obj(LibraryType.class);

		if (level.libraryType == LibraryType.LevelLib) {
			level.unknownHeader = in.i32arr(4);
		} else if (level.libraryType == LibraryType.GfxLib) {
			level.unknownHeader = in.i32arr(3);
		} else {
			level.unknownHeader = in.i32arr(2);
		}

		level.libraryImports = in.objlist(LibraryImport.class);
		level.levelPath = in.str();
		level.libraryObjects = in.objlist(LibraryObject.class);
		level.objectDeclarations = in.objlist(ObjectDeclaration.class);
		level._startcontentoffset = in.pos;

		for (var importObj : level.libraryObjects) {
			if (importObj.libType == LibraryType.GfxLib) {

				GfxLibImport s = new GfxLibImport();
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
				in.enclosing.push(GfxLibImport.class);
				s.comps = in.objlist(Comp.class);
				in.enclosing.pop();

				level.gfxImports.add(s);

			} else throw new IllegalStateException();
		}
		level._endskyboxoffset = in.position();

		boolean searchHeaderBytes = false;

		boolean lastObjectOk = false;

		quit_reading: for (var declaration : level.objectDeclarations) {

			// The previous object had an unknown structure, attempt to skip overr it by
			// finding this object header
			if (searchHeaderBytes) {
				in.i32arr(2); // skip the typical 2 int header to allow proper seek

				searchHeaderBytes = false;

				int[] ints = objectHeaders.get(declaration.type);

				if (ints == null) {
					System.err.println(
							"The last object had unknown structure and we don't this object header, reading cancelled."
									+ declaration.name);
					break quit_reading;
				}

				int seeked = in.seekToi32(ints);
				if (seeked == -1) {
					System.err.println("Seek failed: reading cancelled. " + declaration.name + " at offset "
							+ Integer.toHexString(in.position()));
					break quit_reading;
				} else {
					System.err.println("Skipped 0x" + Integer.toHexString(seeked + 8)); // +8 account for initial header

				}
			}

			lastObjectOk = true;

			System.out.println(declaration.name + " 0x" + Integer.toHexString(in.pos));

			switch (declaration.type) {

			case SequinLeaf:
				level.leafs.put(declaration.name, in.obj(SequinLeaf.class));
				break;
			case Env:
				level.envs.put(declaration.name, in.obj(Env.class));
				break;
			case SequinMaster:
				level.masters.put(declaration.name, in.obj(SequinMaster.class));
				break;
			case DrawGroup:
				level.drawGroups.put(declaration.name, in.obj(DrawGroup.class));
				break;
			case Vibration:
				level.vibs.put(declaration.name, in.obj(Vibration.class));
				break;
			case Bender:
				level.benders.put(declaration.name, in.obj(Bender.class));
				break;
			case PathDecorator:
				level.decorators.put(declaration.name, in.obj(PathDecorator.class));
				break;
			case Mat:
				level.mats.put(declaration.name, in.obj(Mat.class));
				break;
			case DSP:
				level.dsps.put(declaration.name, in.obj(DSP.class));
				break;
			case SequinDrawer:
				level.drawers.put(declaration.name, in.obj(SequinDrawer.class));
				break;
			case ChannelGroup:
				level.channelGroups.put(declaration.name, in.obj(ChannelGroup.class));
				break;
			case SequinPulse:
				level.pulses.put(declaration.name, in.obj(SequinPulse.class));
				break;
			case SequinLevel:
				level.levels.put(declaration.name, in.obj(SequinLevel.class));
				break;
			case Sample:
				level.samples.put(declaration.name, in.obj(Sample.class));
				break;
			case EntitySpawner:
				level.spawners.put(declaration.name, in.obj(EntitySpawner.class));
				break;
			case SequinGate:
				level.gates.put(declaration.name, in.obj(SequinGate.class));
				break;
			case Tex2D:
				level.tex2ds.put(declaration.name, in.obj(Tex2D.class));
				break;
			case Xfmer:
				level.xfmers.put(declaration.name, in.obj(Xfmer.class));
				break;
			case Scene:
				level.scenes.put(declaration.name, in.obj(Scene.class));
				break;
			case Mesh:
				level.meshes.put(declaration.name, in.obj(Mesh.class));
				break;
			case VrSettings:
				level.vrsettings.put(declaration.name, in.obj(VrSettings.class));
				break;
			case TraitAnim:
				level.anims.put(declaration.name, in.obj(TraitAnim.class));
				break;
			case DSPChain:
				level.dchs.put(declaration.name, in.obj(DSPChain.class));
				break;
			case Cam:
				level.cams.put(declaration.name, in.obj(Cam.class));
				break;
			case PostProcessPass:
				level.passes.put(declaration.name, in.obj(PostProcessPass.class));
				break;
			case PostProcess:
				level.postProcess.put(declaration.name, in.obj(PostProcess.class));
				break;
			case Path:
				level.paths.put(declaration.name, in.obj(xyz.anthofoxo.aurora.struct.Path.class));
				break;
			default:

				searchHeaderBytes = true;
				lastObjectOk = false;
				System.err.println("Skipping: " + declaration.name + " (" + declaration.type + ") at 0x"
						+ Integer.toHexString(in.position()));

			}
		}

		if (lastObjectOk)

		{
			System.out.println(
					"Finished reading definitions, footer offset position: 0x" + Integer.toHexString(in.position()));

			level.genericFooter = in.obj(ObjlibFooter.class);

			if (level.libraryType == LibraryType.LevelLib) {

				level.libraryFooter = in.obj(LevelLibFooter.class);

			}

			assert (in.position() == in.bytes.length);
		}
	}

	private void drawParsed() {
		if (level == null) return;

		if (ImGui.collapsingHeader("Samples")) {
			for (var entry : level.samples.entrySet()) {
				if (ImGui.treeNode(entry.getKey())) {
					entry.getValue().gui();
					ImGui.treePop();
				}
			}
		}

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
					ImGui.labelText("type", entry.libType + "");

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

	private record Listing(String name, String path) {
	}

	// @formatter:off
	private static final List<Listing> LEVEL_LISTING = List.of(
			new Listing("Title Screen", "Alevels/title_screen.objlib"),
			new Listing("Level 1", "Alevels/demo.objlib"),
			new Listing("Level 2", "Alevels/level2/level_2a.objlib"),
			new Listing("Level 3", "Alevels/level3/level_3a.objlib"),
			new Listing("Level 4", "Alevels/level4/level_4a.objlib"),
			new Listing("Level 5", "Alevels/level5/level_5a.objlib"),
			new Listing("Level 6", "Alevels/level6/level_6.objlib"),
			new Listing("Level 7", "Alevels/level7/level_7a.objlib"),
			new Listing("Level 8", "Alevels/level8/level_8a.objlib"),
			new Listing("Level 9", "Alevels/level9/level_9a.objlib")
		);
	// @formatter:on

	private void calcLevelLength() {
		if (level == null) return;

		int numBeats = computeBeatLength();
		if (level.libraryFooter != null) {
			if (level.libraryFooter instanceof LevelLibFooter f) {
				float numMinutes = (float) numBeats / f.bpm;
				System.out.println(minutesToMinutesSeconds(numMinutes));
			} else {
				throw new IllegalStateException();
			}

		} else {
			System.out.println(numBeats + " beats");
		}
	}

	public static int getLevelBeatCount(SequinLevel level) {
		int total = 0;

		for (var leafEntry : level.enteries) {
			total += leafEntry.beatCount;
		}

		return total;

	}

	public static String minutesToMinutesSeconds(float minutes) {
		int totalSeconds = Math.round(minutes * 60);
		int mins = totalSeconds / 60;
		int secs = totalSeconds % 60;
		return String.format("%d:%02d", mins, secs);
	}

	public int computeBeatLength() {
		if (level == null) return 0;
		int total = 0;

		// for each master (should be just one)
		for (var master : level.masters.values()) {
			for (var masterLevelEntry : master.levels) {
				if (!masterLevelEntry.lvlName.isEmpty()) {
					var lvl = level.levels.get(masterLevelEntry.lvlName);

					if (lvl == null) {
						System.err.println("cant find " + masterLevelEntry.lvlName);
					} else {
						total += getLevelBeatCount(lvl);
					}

					var lvlrest = level.levels.get(masterLevelEntry.restLvlName);

					if (lvlrest == null) {
						System.err.println("cant find " + masterLevelEntry.restLvlName);
					} else {
						total += getLevelBeatCount(lvlrest);
					}

				} else {

					var gate = level.gates.get(masterLevelEntry.gateName);

					if (gate == null) {
						System.err.println("cant find " + masterLevelEntry.gateName);
						continue;
					}

					for (var pattern : gate.patterns) {
						var lvl = level.levels.get(pattern.levelName);

						if (lvl == null) {
							System.err.println("cant find gate lvl " + pattern.levelName);
						} else {
							total += getLevelBeatCount(lvl);
						}

					}

				}

			}
		}

		return total;
	}

	public void draw() {
		if (!visible.get()) return;

		if (Hasher.commExport != null) {
			input.set(UserConfig.thumperPath() + "/cache/" + Hasher.commExport + ".pc");
			Hasher.commExport = null;
		}

		if (ImGui.begin("Objlib Decomp", visible)) {
			String prefix = UserConfig.thumperPath() + "/cache/";

			for (var entry : LEVEL_LISTING) {
				if (ImGui.button(entry.name)) {
					input.set(prefix + Integer.toHexString(Hash.fnv1a(entry.path)) + ".pc");
				}
				ImGui.sameLine();
			}

			ImGui.inputText("Open Objlib", input);

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

			if (ImGui.button("Calc Length")) {
				calcLevelLength();
			}

		}
		ImGui.end();
	}
}
