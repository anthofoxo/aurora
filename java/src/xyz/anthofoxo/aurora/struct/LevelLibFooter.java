package xyz.anthofoxo.aurora.struct;

import static xyz.anthofoxo.aurora.struct.trait.TraitBucket.kBucketParent;
import static xyz.anthofoxo.aurora.struct.trait.TraitConstraint.kConstraintParent;
import static xyz.anthofoxo.aurora.struct.trait.TraitLayer.kNumDrawLayers;

import java.util.List;

import xyz.anthofoxo.aurora.struct.comp.DrawComp;
import xyz.anthofoxo.aurora.struct.comp.XfmComp;

public class LevelLibFooter implements ThumperStruct {
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

	public static LevelLibFooter ofTmlDefaults(float bpm) {
		LevelLibFooter instance = new LevelLibFooter();

		instance.camName = "world.cam";

		instance.camObject = new Cam(
				List.of(new DrawComp(8, true, kNumDrawLayers, kBucketParent, List.of()),
						new XfmComp(1, "", kConstraintParent, Transform.identity())),
				new Vec3f((float) Math.toRadians(45.0), 1f, 1000f));
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
