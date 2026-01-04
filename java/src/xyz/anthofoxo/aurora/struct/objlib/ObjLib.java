package xyz.anthofoxo.aurora.struct.objlib;

import static xyz.anthofoxo.aurora.struct.objlib.DeclarationType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.anthofoxo.aurora.gui.FileType;
import xyz.anthofoxo.aurora.parse.AuroraReader;
import xyz.anthofoxo.aurora.parse.AuroraWriter;
import xyz.anthofoxo.aurora.struct.Bender;
import xyz.anthofoxo.aurora.struct.Cam;
import xyz.anthofoxo.aurora.struct.ChannelGroup;
import xyz.anthofoxo.aurora.struct.DSP;
import xyz.anthofoxo.aurora.struct.DSPChain;
import xyz.anthofoxo.aurora.struct.DrawGroup;
import xyz.anthofoxo.aurora.struct.EntitySpawner;
import xyz.anthofoxo.aurora.struct.Env;
import xyz.anthofoxo.aurora.struct.GfxLibImport;
import xyz.anthofoxo.aurora.struct.GfxLibImport.Grouping;
import xyz.anthofoxo.aurora.struct.LibraryObject;
import xyz.anthofoxo.aurora.struct.LibraryType;
import xyz.anthofoxo.aurora.struct.Mat;
import xyz.anthofoxo.aurora.struct.Mesh;
import xyz.anthofoxo.aurora.struct.Path;
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
import xyz.anthofoxo.aurora.struct.ThumperStruct;
import xyz.anthofoxo.aurora.struct.TraitAnim;
import xyz.anthofoxo.aurora.struct.Vibration;
import xyz.anthofoxo.aurora.struct.VrSettings;
import xyz.anthofoxo.aurora.struct.Xfmer;
import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;
import xyz.anthofoxo.aurora.struct.sequin.ParamPath;

public class ObjLib implements ThumperStruct {
	public FileType fileType;
	public LibraryType libraryType;
	@FixedSize(count = 2)
	public int[] header;
	public List<LibraryImport> libraryImports;
	public String path;
	public List<LibraryObject> libraryObjects;
	public List<ObjectDeclaration> objectDeclarations;
	public List<ThumperStruct> objectDefinitions;
	public int unknown0;
	public List<Comp> comps;

	private static final Map<DeclarationType, Class<? extends ThumperStruct>> STRUCTS;

	static {
		STRUCTS = new HashMap<>();
		STRUCTS.put(SequinMaster, SequinMaster.class);
		STRUCTS.put(SequinDrawer, SequinDrawer.class);
		STRUCTS.put(SequinLevel, SequinLevel.class);
		STRUCTS.put(SequinGate, SequinGate.class);
		STRUCTS.put(SequinLeaf, SequinLeaf.class);
		STRUCTS.put(SequinPulse, SequinPulse.class);
		STRUCTS.put(TraitAnim, TraitAnim.class);
		STRUCTS.put(DSP, DSP.class);
		STRUCTS.put(DSPChain, DSPChain.class);
		STRUCTS.put(ChannelGroup, ChannelGroup.class);
		STRUCTS.put(DrawGroup, DrawGroup.class);
		STRUCTS.put(Env, Env.class);
		STRUCTS.put(Vibration, Vibration.class);
		STRUCTS.put(Bender, Bender.class);
		STRUCTS.put(PathDecorator, PathDecorator.class);
		STRUCTS.put(Mat, Mat.class);
		STRUCTS.put(Sample, Sample.class);
		STRUCTS.put(EntitySpawner, EntitySpawner.class);
		STRUCTS.put(Tex2D, Tex2D.class);
		STRUCTS.put(Xfmer, Xfmer.class);
		STRUCTS.put(Scene, Scene.class);
		STRUCTS.put(Mesh, Mesh.class);
		STRUCTS.put(VrSettings, VrSettings.class);
		STRUCTS.put(Cam, Cam.class);
		STRUCTS.put(PostProcessPass, PostProcessPass.class);
		STRUCTS.put(PostProcess, PostProcess.class);
		STRUCTS.put(Path, Path.class);
	}

	public static ObjLib in(AuroraReader in) {
		var instance = new ObjLib();
		instance.fileType = in.obj(FileType.class);
		assert (instance.fileType == FileType.ObjLib);
		instance.libraryType = in.obj(LibraryType.class);
		assert (instance.libraryType == LibraryType.ObjLib);
		instance.header = in.i32arr(2);
		instance.libraryImports = in.objlist(LibraryImport.class);
		instance.path = in.str();
		instance.libraryObjects = in.objlist(LibraryObject.class);
		instance.objectDeclarations = in.objlist(ObjectDeclaration.class);

		instance.objectDefinitions = new ArrayList<>();

		for (var libraryObjectDeclaration : instance.libraryObjects) {
			assert (libraryObjectDeclaration.libType == LibraryType.GfxLib);

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

			in.enclosing.push(GfxLibImport.class);
			s.comps = in.objlist(Comp.class);
			in.enclosing.pop();

			instance.objectDefinitions.add(s);
		}

		for (var objectDeclaration : instance.objectDeclarations) {
			var clazz = STRUCTS.get(objectDeclaration.type);

			if (clazz == null) {
				throw new IllegalStateException("Unknown object body: " + objectDeclaration.type);
			}

			instance.objectDefinitions.add(in.obj(clazz));
		}

		instance.unknown0 = in.i32();
		instance.comps = in.objlist(Comp.class);
		return instance;
	}

	public static void out(AuroraWriter out, ObjLib instance) {
		assert (instance.libraryObjects.size() + instance.objectDeclarations.size() == instance.objectDefinitions
				.size());

		out.obj(instance.fileType);
		out.obj(instance.libraryType);
		out.i32arr(instance.header);
		out.objlist(instance.libraryImports);
		out.str(instance.path);
		out.objlist(instance.libraryObjects);
		out.objlist(instance.objectDeclarations);
		for (var object : instance.objectDefinitions) out.obj(object);
		out.i32(instance.unknown0);
		out.objlist(instance.comps);
	}
}
