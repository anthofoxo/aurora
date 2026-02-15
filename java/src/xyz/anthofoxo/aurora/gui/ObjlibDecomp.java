package xyz.anthofoxo.aurora.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import imgui.ImGui;
import imgui.ImGuiTextFilter;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
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
import xyz.anthofoxo.aurora.struct.EntityAnim;
import xyz.anthofoxo.aurora.struct.EntitySpawner;
import xyz.anthofoxo.aurora.struct.EntityVar;
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
import xyz.anthofoxo.aurora.struct.Vec4f;
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
	private ImString input = new ImString("Adecorators/turn.objlib", 512);
	private String error = "";

	public static class ObjlibObject {
		public String name;
		public DeclarationType type;
		public Object definition;
		public boolean editorEnabled;

		public int offsetStart;
		public int offsetEnd;
		public int offsetSize;
	}

	static ImGuiTextFilter filter = new ImGuiTextFilter();

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

		/*
		 * public Map<String, xyz.anthofoxo.aurora.struct.Path> paths = new HashMap<>();
		 * public Map<String, SequinLeaf> leafs = new HashMap<>(); public Map<String,
		 * SequinMaster> masters = new HashMap<>(); public Map<String, SequinLevel>
		 * levels = new HashMap<>(); public Map<String, SequinGate> gates = new
		 * HashMap<>(); public Map<String, Cam> cams = new HashMap<>(); public
		 * Map<String, Bender> benders = new HashMap<>(); public Map<String, Env> envs =
		 * new HashMap<>(); public Map<String, SequinDrawer> drawers = new HashMap<>();
		 * public Map<String, Sample> samples = new HashMap<>(); public Map<String,
		 * TraitAnim> anims = new HashMap<>(); public Map<String, EntityVar> entityVars
		 * = new HashMap<>(); public Map<String, Mesh> meshes = new HashMap<>(); public
		 * Map<String, VrSettings> vrsettings = new HashMap<>(); public Map<String,
		 * EntitySpawner> spawners = new HashMap<>(); public Map<String, Tex2D> tex2ds =
		 * new HashMap<>(); public Map<String, Xfmer> xfmers = new HashMap<>(); public
		 * Map<String, ChannelGroup> channelGroups = new HashMap<>(); public Map<String,
		 * DSPChain> dchs = new HashMap<>(); public Map<String, Vibration> vibs = new
		 * HashMap<>(); public Map<String, PathDecorator> decorators = new HashMap<>();
		 * public Map<String, Scene> scenes = new HashMap<>(); public Map<String,
		 * PostProcess> postProcess = new HashMap<>(); public Map<String,
		 * PostProcessPass> passes = new HashMap<>(); public Map<String, SequinPulse>
		 * pulses = new HashMap<>(); public Map<String, Mat> mats = new HashMap<>();
		 * public Map<String, DSP> dsps = new HashMap<>(); public Map<String, DrawGroup>
		 * drawGroups = new HashMap<>();
		 */

		public List<ObjlibObject> objects = new ArrayList<>();

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
		objectHeaders.put(DeclarationType.EntityVar, EntityVar.header());
		objectHeaders.put(DeclarationType.EntityAnim, EntityAnim.header());
	}

	private static ImString buffer = new ImString(512);

	private void panelProjectExplorer() {
		if (level == null) return;

		var imFloat = new ImFloat();
		var imInt = new ImInt();
		var imBool = new ImBoolean();

		for (var object : level.objects) {
			if (!object.editorEnabled) continue;

			if (object.type == DeclarationType.SequinLevel && object.definition instanceof SequinLevel obj) {
				ImBoolean visible = new ImBoolean(object.editorEnabled);

				if (ImGui.begin("SequinLevel Editor (" + object.name + ")", visible)) {

					int i = 0;
					for (var entry : obj.enteries) {
						if (ImGui.treeNode(i++, entry.leafName)) {

							ImGui.labelText("unknown0", entry.unknown0 + "");
							ImGui.labelText("beatCount", entry.beatCount + "");
							ImGui.labelText("unknown1", entry.unknown1 + "");
							ImGui.labelText("leafName", entry.leafName + "");
							ImGui.labelText("mainPath", entry.mainPath + "");
							ImGui.labelText("stepGameplay", entry.stepGameplay + "");
							ImGui.labelText("totalBeatToThisPoint", entry.totalBeatToThisPoint + "");

							if (ImGui.collapsingHeader("Subpaths")) {
								for (var subpathEntry : entry.subpaths) {
									int j = 0;
									if (ImGui.treeNode(j++, subpathEntry.path)) {
										ImGui.treePop();
									}
								}
							}

							ImGui.treePop();
						}

					}

				}
				ImGui.end();

				if (!visible.get()) {
					object.editorEnabled = false;
				}
			}

			if (object.type == DeclarationType.Sample && object.definition instanceof Sample obj) {

				ImBoolean visible = new ImBoolean(object.editorEnabled);

				if (ImGui.begin("Sample Editor (" + object.name + ")", visible)) {

					buffer.set(obj.path);
					if (ImGui.inputText("Path", buffer)) {
						obj.path = buffer.get();
					}

					buffer.set(obj.channelGroup);
					if (ImGui.inputText("Channel Group", buffer)) {
						obj.channelGroup = buffer.get();
					}

					buffer.set(obj.mode);
					if (ImGui.inputText("Mode", buffer)) {
						obj.mode = buffer.get();
					}

					var v = new ImFloat(obj.offset);
					if (ImGui.inputFloat("Offset", v)) obj.offset = v.get();

					v = new ImFloat(obj.pan);
					if (ImGui.inputFloat("Pan", v)) obj.pan = v.get();

					v = new ImFloat(obj.pitch);
					if (ImGui.inputFloat("Pitch", v)) obj.pitch = v.get();

					v = new ImFloat(obj.volume);
					if (ImGui.inputFloat("Volume", v)) obj.volume = v.get();

				}
				ImGui.end();

				if (!visible.get()) {
					object.editorEnabled = false;
				}

			}

			if (object.definition instanceof SequinGate obj) {
				ImBoolean visible = new ImBoolean(object.editorEnabled);

				if (ImGui.begin("Gate Editor (" + object.name + ")", visible)) {

					buffer.set(obj.entitySpawnerName);
					if (ImGui.inputText("Name", buffer)) {
						obj.entitySpawnerName = buffer.get();
					}

					buffer.set(obj.preLevelName);
					if (ImGui.inputText("preLevelName", buffer)) {
						obj.preLevelName = buffer.get();
					}

					buffer.set(obj.postLevelName);
					if (ImGui.inputText("postLevelName", buffer)) {
						obj.postLevelName = buffer.get();
					}

					buffer.set(obj.restartLevelName);
					if (ImGui.inputText("restartLevelName", buffer)) {
						obj.restartLevelName = buffer.get();
					}

					buffer.set(obj.unknown0);
					if (ImGui.inputText("pellet transition level", buffer)) {
						obj.unknown0 = buffer.get();
					}

					buffer.set(obj.sectionType);
					if (ImGui.inputText("sectionType", buffer)) {
						obj.sectionType = buffer.get();
					}

					buffer.set(obj.randomType);
					if (ImGui.inputText("randomType", buffer)) {
						obj.randomType = buffer.get();
					}

					imFloat.set(obj.unknown1);
					if (ImGui.inputFloat("unknown9", imFloat)) obj.unknown1 = imFloat.get();

					ImGui.separatorText("Params");

					for (var param : obj.params) {
						var t = Hash.hashes.get(param.paramHash);
						var str = t == null ? Integer.toHexString(param.paramHash) : new String(t);
						ImGui.text(str + " (" + param.paramIdx + ")");
					}

					ImGui.separatorText("Entries");

					int i = 0;
					for (var entry : obj.patterns) {
						if (ImGui.treeNode(i++, "(" + i + ") " + entry.levelName)) {

							buffer.set(entry.levelName);
							if (ImGui.inputText("levelName", buffer)) {
								entry.levelName = buffer.get();
							}

							buffer.set(entry.sentryType);
							if (ImGui.inputText("sentryType", buffer)) {
								entry.sentryType = buffer.get();
							}

							imBool.set(entry.unknown0);
							if (ImGui.checkbox("unknown0", imBool)) entry.unknown0 = imBool.get();

							imInt.set(entry.unknown1);
							if (ImGui.inputInt("unknown1", imInt)) entry.unknown1 = imInt.get();
							imInt.set(entry.nodeHash);
							if (ImGui.inputInt("nodeHash", imInt)) entry.nodeHash = imInt.get();
							imInt.set(entry.bucketNum);
							if (ImGui.inputInt("Bucket Number", imInt)) entry.bucketNum = imInt.get();

							ImGui.treePop();
						}
					}

				}
				ImGui.end();

				if (!visible.get()) {
					object.editorEnabled = false;
				}
			}

			if (object.type == DeclarationType.EntitySpawner && object.definition instanceof EntitySpawner obj) {

				ImBoolean visible = new ImBoolean(object.editorEnabled);

				if (ImGui.begin("Entity Spawner Editor (" + object.name + ")", visible)) {

					imInt.set(obj.unknown);
					if (ImGui.inputInt("unknown", imInt)) obj.unknown = imInt.get();

					buffer.set(obj.objlibPath);
					if (ImGui.inputText("objlibPath", buffer)) {
						obj.objlibPath = buffer.get();
					}

					buffer.set(obj.bucket);
					if (ImGui.inputText("bucket", buffer)) {
						obj.bucket = buffer.get();
					}

				}
				ImGui.end();

				if (!visible.get()) {
					object.editorEnabled = false;
				}

			}

			if (object.type == DeclarationType.SequinMaster && object.definition instanceof SequinMaster obj) {

				ImBoolean visible = new ImBoolean(object.editorEnabled);

				if (ImGui.begin("Master Editor (" + object.name + ")", visible)) {

					// header, comp

					imInt.set(obj.unknown4);
					if (ImGui.inputInt("Unknown4", imInt)) obj.unknown4 = imInt.get();

					imFloat.set(obj.unknown5);
					if (ImGui.inputFloat("unknown5", imFloat)) obj.unknown5 = imFloat.get();

					buffer.set(obj.skybox);
					if (ImGui.inputText("Skybox", buffer)) obj.skybox = buffer.get();

					buffer.set(obj.introLevel);
					if (ImGui.inputText("Intro Level", buffer)) {
						obj.introLevel = buffer.get();
					}

					imBool.set(obj.footer1);
					if (ImGui.checkbox("footer1", imBool)) obj.footer1 = imBool.get();
					imBool.set(obj.footer2);
					if (ImGui.checkbox("footer2", imBool)) obj.footer2 = imBool.get();
					imInt.set(obj.footer3);
					if (ImGui.inputInt("footer3", imInt)) obj.footer3 = imInt.get();
					imInt.set(obj.footer4);
					if (ImGui.inputInt("footer4", imInt)) obj.footer4 = imInt.get();
					imInt.set(obj.footer5);
					if (ImGui.inputInt("footer5", imInt)) obj.footer5 = imInt.get();
					imInt.set(obj.footer6);
					if (ImGui.inputInt("footer6", imInt)) obj.footer6 = imInt.get();
					imFloat.set(obj.footer7);
					if (ImGui.inputFloat("footer7", imFloat)) obj.footer7 = imFloat.get();
					imFloat.set(obj.footer8);
					if (ImGui.inputFloat("footer8", imFloat)) obj.footer8 = imFloat.get();
					imFloat.set(obj.footer9);
					if (ImGui.inputFloat("footer9", imFloat)) obj.footer9 = imFloat.get();

					buffer.set(obj.checkpointLvl);
					if (ImGui.inputText("Checkpoint Level", buffer)) {
						obj.checkpointLvl = buffer.get();
					}

					buffer.set(obj.pathGameplay);
					if (ImGui.inputText("Gameplay", buffer)) {
						obj.pathGameplay = buffer.get();
					}

					if (ImGui.collapsingHeader("Entries")) {

						int i = 0;

						for (var entry : obj.levels) {

							var display = entry.gateName.isEmpty() ? entry.lvlName : entry.gateName;

							if (ImGui.treeNode(i++, "(" + i + ") " + display)) {

								buffer.set(entry.lvlName);
								if (ImGui.inputText("lvlName", buffer)) entry.lvlName = buffer.get();
								buffer.set(entry.gateName);
								if (ImGui.inputText("gateName", buffer)) entry.gateName = buffer.get();
								imBool.set(entry.hasCheckpoint);
								if (ImGui.checkbox("Checkpoint", imBool)) entry.hasCheckpoint = imBool.get();

								buffer.set(entry.checkpointLeaderLvlName);
								if (ImGui.inputText("checkpointLeaderLvlName", buffer))
									entry.checkpointLeaderLvlName = buffer.get();
								buffer.set(entry.restLvlName);
								if (ImGui.inputText("restLvlName", buffer)) entry.restLvlName = buffer.get();

								imBool.set(entry.unknownBool0);
								if (ImGui.checkbox("unknownBool0", imBool)) entry.unknownBool0 = imBool.get();
								imBool.set(entry.unknownBool1);
								if (ImGui.checkbox("unknownBool1", imBool)) entry.unknownBool1 = imBool.get();

								imInt.set(entry.unknown0);
								if (ImGui.inputInt("unknown0", imInt)) entry.unknown0 = imInt.get();

								imBool.set(entry.unknownBool2);
								if (ImGui.checkbox("unknownBool2", imBool)) entry.unknownBool2 = imBool.get();
								imBool.set(entry.playPlus);
								if (ImGui.checkbox("Play Plus", imBool)) entry.playPlus = imBool.get();

								ImGui.treePop();
							}
						}
					}

				}
				ImGui.end();

				if (!visible.get()) {
					object.editorEnabled = false;
				}

			}
		}

		if (!ImGui.begin("Project Explorer")) {
			ImGui.end();
			return;
		}

		ImGui.textUnformatted(
				level.levelPath + " (" + Integer.toHexString(Hash.fnv1a("A" + level.levelPath.toLowerCase())) + ".pc)");
		ImGui.separator();

		filter.draw();

		for (var entry : level.objects) {
			if (!filter.passFilter(entry.name)) continue;

			if (ImGui.selectable(entry.name, entry.editorEnabled)) {
				entry.editorEnabled ^= true;
			}

			ImGui.setItemTooltip("Offset: 0x" + Integer.toHexString(entry.offsetStart) + ", Size: 0x"
					+ Integer.toHexString(entry.offsetSize));
		}

		ImGui.end();
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
					} else if (g.type.equals("kTraitAction")) {
						in.enclosing.push(GfxLibImport.class);
						in.objlist(Comp.class);
						in.enclosing.pop();
					} else throw new IllegalStateException(g.type + " not supported");

					s.groupings.add(g);
				}

				// when parsing a skybox, drawcomp needs one field stripped, ensure the parser
				// knows this dependency
				in.enclosing.push(GfxLibImport.class);
				s.comps = in.objlist(Comp.class);
				in.enclosing.pop();

				level.gfxImports.add(s);

			} else if (importObj.libType == LibraryType.AvatarLib) {

				in.i32arr(3);
				in.bool();
				int groupNum = in.i32(); // groups num

				for (int i = 0; i < groupNum; ++i) {

					in.objlist(ParamPath.class); // param paths
					String traittype = in.str(); // traittype

					if (traittype.equals("kTraitObj")) {
						in.str();
					} else if (traittype.equals("kTraitBool")) {
						in.bool();
					} else if (traittype.equals("kTraitColor")) {
						in.obj(Vec4f.class);
					} else if (traittype.equals("kTraitFloat")) {
						in.f32();
					} else if (traittype.equals("kTraitAction")) {
						in.enclosing.push(GfxLibImport.class);
						in.objlist(Comp.class);
						in.enclosing.pop();
					} else throw new IllegalStateException(traittype + " not supported");

				}

			} else throw new IllegalStateException("unknown import type " + importObj.libType);
		}
		level._endskyboxoffset = in.position();

		boolean searchHeaderBytes = false;

		boolean lastObjectOk = false;

		ObjlibObject lastRefFilled = null;

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

					if (lastRefFilled != null) {
						lastRefFilled.offsetEnd = in.pos;
						lastRefFilled.offsetSize = lastRefFilled.offsetEnd - lastRefFilled.offsetStart;

					}
				}
			}

			lastObjectOk = true;

			System.out.println(declaration.name + " 0x" + Integer.toHexString(in.pos));

			var obj = new ObjlibObject();
			obj.type = declaration.type;
			obj.name = declaration.name;

			obj.offsetStart = in.pos;

			switch (declaration.type) {

			case SequinLeaf:
				obj.definition = in.obj(SequinLeaf.class);
				break;
			case Env:
				obj.definition = in.obj(Env.class);
				break;
			case SequinMaster:
				obj.definition = in.obj(SequinMaster.class);
				break;
			case DrawGroup:
				obj.definition = in.obj(DrawGroup.class);
				break;
			case Vibration:
				obj.definition = in.obj(Vibration.class);
				break;
			case Bender:
				obj.definition = in.obj(Bender.class);
				break;
			case EntityVar:
				obj.definition = in.obj(EntityVar.class);
				break;
			case PathDecorator:
				obj.definition = in.obj(PathDecorator.class);
				break;
			case Mat:
				obj.definition = in.obj(Mat.class);
				break;
			case DSP:
				obj.definition = in.obj(DSP.class);
				break;
			case SequinDrawer:
				obj.definition = in.obj(SequinDrawer.class);
				break;
			case ChannelGroup:
				obj.definition = in.obj(ChannelGroup.class);
				break;
			case SequinPulse:
				obj.definition = in.obj(SequinPulse.class);
				break;
			case SequinLevel:
				obj.definition = in.obj(SequinLevel.class);
				break;
			case Sample:
				obj.definition = in.obj(Sample.class);
				break;
			case EntitySpawner:
				obj.definition = in.obj(EntitySpawner.class);
				break;
			case SequinGate:
				obj.definition = in.obj(SequinGate.class);
				break;
			case Tex2D:
				obj.definition = in.obj(Tex2D.class);
				break;
			case Xfmer:
				obj.definition = in.obj(Xfmer.class);
				break;
			case Scene:
				obj.definition = in.obj(Scene.class);
				break;
			case Mesh:
				obj.definition = in.obj(Mesh.class);
				break;
			case VrSettings:
				obj.definition = in.obj(VrSettings.class);
				break;
			case TraitAnim:
				obj.definition = in.obj(TraitAnim.class);
				break;
			case DSPChain:
				obj.definition = in.obj(DSPChain.class);
				break;
			case Cam:
				obj.definition = in.obj(Cam.class);
				break;
			case PostProcessPass:
				obj.definition = in.obj(PostProcessPass.class);
				break;
			case PostProcess:
				obj.definition = in.obj(PostProcess.class);
				break;
			case Path:
				obj.definition = in.obj(xyz.anthofoxo.aurora.struct.Path.class);
				break;
			default:

				searchHeaderBytes = true;
				lastObjectOk = false;
				System.err.println("Skipping: " + declaration.name + " (" + declaration.type + ") at 0x"
						+ Integer.toHexString(in.position()));

				lastRefFilled = obj;
			}

			obj.offsetEnd = in.pos;
			obj.offsetSize = obj.offsetEnd - obj.offsetStart;

			level.objects.add(obj);
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

		// export flows

		for (var obj : level.objects) {
			if (obj.type == DeclarationType.Flow) {

				String file = "C:\\Users\\antho\\Desktop\\testing\\"
						+ level.levelPath.replace('.', '_').replace('/', '_') + "#" + obj.name;

				byte[] data = Arrays.copyOfRange(bytes, obj.offsetStart, obj.offsetEnd);

				Files.write(Path.of(file), data);

			}
		}

	}

	private void drawParsed() {
		if (level == null) return;

		/*
		 * if (ImGui.collapsingHeader("Samples")) { for (var entry :
		 * level.samples.entrySet()) { if (ImGui.treeNode(entry.getKey())) {
		 * entry.getValue().gui(); ImGui.treePop(); } } }
		 */

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

	/*
	 * 
	 * private void calcLevelLength() { if (level == null) return;
	 * 
	 * int numBeats = computeBeatLength(); if (level.libraryFooter != null) { if
	 * (level.libraryFooter instanceof LevelLibFooter f) { float numMinutes =
	 * (float) numBeats / f.bpm;
	 * System.out.println(minutesToMinutesSeconds(numMinutes)); } else { throw new
	 * IllegalStateException(); }
	 * 
	 * } else { System.out.println(numBeats + " beats"); } }
	 * 
	 * public static int getLevelBeatCount(SequinLevel level) { int total = 0;
	 * 
	 * for (var leafEntry : level.enteries) { total += leafEntry.beatCount; }
	 * 
	 * return total;
	 * 
	 * }
	 * 
	 * public static String minutesToMinutesSeconds(float minutes) { int
	 * totalSeconds = Math.round(minutes * 60); int mins = totalSeconds / 60; int
	 * secs = totalSeconds % 60; return String.format("%d:%02d", mins, secs); }
	 * 
	 * public int computeBeatLength() { if (level == null) return 0; int total = 0;
	 * 
	 * // for each master (should be just one) for (var master :
	 * level.masters.values()) { for (var masterLevelEntry : master.levels) { if
	 * (!masterLevelEntry.lvlName.isEmpty()) { var lvl =
	 * level.levels.get(masterLevelEntry.lvlName);
	 * 
	 * if (lvl == null) { System.err.println("cant find " +
	 * masterLevelEntry.lvlName); } else { total += getLevelBeatCount(lvl); }
	 * 
	 * var lvlrest = level.levels.get(masterLevelEntry.restLvlName);
	 * 
	 * if (lvlrest == null) { System.err.println("cant find " +
	 * masterLevelEntry.restLvlName); } else { total += getLevelBeatCount(lvlrest);
	 * }
	 * 
	 * } else {
	 * 
	 * var gate = level.gates.get(masterLevelEntry.gateName);
	 * 
	 * if (gate == null) { System.err.println("cant find " +
	 * masterLevelEntry.gateName); continue; }
	 * 
	 * for (var pattern : gate.patterns) { var lvl =
	 * level.levels.get(pattern.levelName);
	 * 
	 * if (lvl == null) { System.err.println("cant find gate lvl " +
	 * pattern.levelName); } else { total += getLevelBeatCount(lvl); }
	 * 
	 * }
	 * 
	 * }
	 * 
	 * } }
	 * 
	 * return total; }
	 */

	public void draw() {
		if (!visible.get()) return;

		panelProjectExplorer();

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

			/*
			 * if (ImGui.button("Calc Length")) { calcLevelLength(); }
			 */

			/*
			 * if (ImGui.button("Extract to .aur format")) {
			 * 
			 * var root = new YAMLMapper().createObjectNode();
			 * 
			 * for (var entry : level.masters.entrySet()) { var node =
			 * entry.getValue().toAur(); root.set(entry.getKey(), node); }
			 * 
			 * for (var entry : level.gates.entrySet()) { var node =
			 * entry.getValue().toAur(); root.set(entry.getKey(), node); }
			 * 
			 * for (var entry : level.levels.entrySet()) { var node =
			 * entry.getValue().toAur(); root.set(entry.getKey(), node); }
			 * 
			 * ObjectMapper jsonMapper = new ObjectMapper(); Object data =
			 * jsonMapper.convertValue(root, Object.class);
			 * 
			 * DumpSettings settings =
			 * DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.PLAIN)
			 * .setDefaultFlowStyle(FlowStyle.BLOCK).setIndent(2).setIndicatorIndent(2) //
			 * dash alignment .setIndentWithIndicator(true) // prettier lists .setWidth(120)
			 * // avoid aggressive wrapping .build();
			 * 
			 * Dump dump = new Dump(settings); String yaml = dump.dumpToString(data);
			 * 
			 * var bytes = yaml.getBytes(); var buffer = MemoryUtil.memAlloc(bytes.length);
			 * buffer.put(0, bytes); GLFW.nglfwSetClipboardString(EntryPoint.window,
			 * MemoryUtil.memAddress(buffer)); MemoryUtil.memFree(buffer); }
			 */

		}
		ImGui.end();
	}
}
