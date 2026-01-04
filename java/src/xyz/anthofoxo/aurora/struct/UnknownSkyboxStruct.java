package xyz.anthofoxo.aurora.struct;

import java.util.List;

import xyz.anthofoxo.aurora.struct.annotation.FixedSize;
import xyz.anthofoxo.aurora.struct.comp.Comp;
import xyz.anthofoxo.aurora.struct.comp.DrawComp;
import xyz.anthofoxo.aurora.struct.comp.EditStateComp;
import xyz.anthofoxo.aurora.struct.comp.PollComp;
import xyz.anthofoxo.aurora.struct.comp.XfmComp;
import xyz.anthofoxo.aurora.struct.sequin.ParamPath;

public class UnknownSkyboxStruct implements ThumperStruct {

	public static class Grouping implements ThumperStruct {
		public List<ParamPath> params;
		public String type;
		public Object value;

		public Grouping() {
		}

		public Grouping(List<ParamPath> params, String type, Object value) {
			this.params = params;
			this.type = type;
			this.value = value;
		}

	}

	public static int[] header() {
		return new int[] { 19, 21 };
	}

	@FixedSize(count = 2)
	public int[] header;
	public boolean unknown0 = true;

	public List<Grouping> groupings;

	/**
	 * Skyboxes typically have {@link PollComp}, {@link EditStateComp},
	 * {@link DrawComp}, and {@link XfmComp}
	 */
	public List<Comp> comps;
}
