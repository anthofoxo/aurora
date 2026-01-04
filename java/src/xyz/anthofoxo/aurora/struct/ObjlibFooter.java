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

public class ObjlibFooter implements ThumperStruct {
	public int unknown0;
	public List<Comp> comps;

	public static ObjlibFooter ofTmlDefaults() {
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
				

		
		return instance;
	}

}
