package xyz.anthofoxo.aurora.struct;

import static xyz.anthofoxo.aurora.struct.trait.TraitBucket.kBucketParent;
import static xyz.anthofoxo.aurora.struct.trait.TraitConstraint.kConstraintParent;
import static xyz.anthofoxo.aurora.struct.trait.TraitLayer.kNumDrawLayers;

import java.util.List;

import xyz.anthofoxo.aurora.struct.comp.Comp;
import xyz.anthofoxo.aurora.struct.comp.DrawComp;
import xyz.anthofoxo.aurora.struct.comp.EditStateComp;
import xyz.anthofoxo.aurora.struct.comp.PollComp;
import xyz.anthofoxo.aurora.struct.comp.XfmComp;
import xyz.anthofoxo.aurora.struct.experimental.Cam;

public class ObjlibFooter implements ThumperStruct {
	public int unknown0;
	public List<Comp> comps;
	public String camName;
	public Cam camObject;
	public String scene;
	public String lowspecScene;
	public String gameplayVrSettings;
	public String environment;
	public String cameraRef0;
	public String nxCamera;
	public String cameraRef1;
	public float bpm;
	public String avatarLib;
	public String sequinMaster;
	public String sequinDrawer;
	public String masterCh;
	public String baseCh;
	public String timing;
	public boolean unknown1;

	public static ObjlibFooter ofTmlDefaults(float bpm) {
		ObjlibFooter instance = new ObjlibFooter();
		instance.unknown0 = 0;

		// @formatter:off
		instance.comps = List.of(
				new PollComp(0),
				new EditStateComp(),
				new DrawComp(
						8, true, kNumDrawLayers, kBucketParent,
						List.of("sequin.drawer", "avatar.lib")),
				new XfmComp(1, "", kConstraintParent, Transform.identity())
			);
				

		instance.camName = "world.cam";
		
		instance.camObject = new Cam(List.of(
				new DrawComp(8, true, kNumDrawLayers, kBucketParent, List.of()),
				new XfmComp(1, "", kConstraintParent, Transform.identity())
				
				// 0.785398185253143f
				// pi / 4
				// 45deg
				// radians(45deg)
			), new Vec3f((float) Math.toRadians(45.0), 1f, 1000f)); 
		// @formatter:on

		instance.scene = "scene";
		instance.lowspecScene = "low_spec.scene";
		instance.gameplayVrSettings = "gameplay.vr_settings";
		instance.environment = "world.env";
		instance.cameraRef0 = "world.cam";
		instance.nxCamera = "nx.cam";
		instance.cameraRef1 = "world.cam";
		instance.bpm = bpm;
		instance.avatarLib = "avatar.lib";
		instance.sequinMaster = "sequin.master";
		instance.sequinDrawer = "sequin.drawer";
		instance.masterCh = "master.ch";
		instance.baseCh = "base.ch";
		instance.timing = "master_realtime.ch";
		instance.unknown1 = true;
		return instance;
	}

}
