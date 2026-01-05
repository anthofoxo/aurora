package xyz.anthofoxo.aurora.parse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import xyz.anthofoxo.aurora.Util;
import xyz.anthofoxo.aurora.struct.LibraryObject;
import xyz.anthofoxo.aurora.struct.LibraryType;
import xyz.anthofoxo.aurora.struct.Transform;
import xyz.anthofoxo.aurora.struct.GfxLibImport;
import xyz.anthofoxo.aurora.struct.GfxLibImport.Grouping;
import xyz.anthofoxo.aurora.struct.comp.DrawComp;
import xyz.anthofoxo.aurora.struct.comp.EditStateComp;
import xyz.anthofoxo.aurora.struct.comp.PollComp;
import xyz.anthofoxo.aurora.struct.comp.XfmComp;
import xyz.anthofoxo.aurora.struct.objlib.DeclarationType;
import xyz.anthofoxo.aurora.struct.objlib.LibraryImport;
import xyz.anthofoxo.aurora.struct.objlib.ObjectDeclaration;
import xyz.anthofoxo.aurora.struct.sequin.ParamPath;
import xyz.anthofoxo.aurora.struct.trait.TraitBucket;
import xyz.anthofoxo.aurora.struct.trait.TraitConstraint;
import xyz.anthofoxo.aurora.struct.trait.TraitLayer;

public final class PrecompiledBin {
	private PrecompiledBin() {
	}

	public static byte[] getHeaderBin() {
		AuroraWriter out = new AuroraWriter();
		out.i32(8); // FileType // ObjLib
		out.i32(0x0B374D9E); // ObjLib Type // Level
		out.i32arr(33, 19, 21, 4); // Unknown header values
		// @formatter:off
		out.objlist(Arrays.asList(
				new LibraryImport(0, "global.objlib"),
				new LibraryImport(0, "global/rises_once.objlib"),
				new LibraryImport(0, "global/global_scaffolds/scaffolds.objlib"),
				new LibraryImport(0, "global/gongs.objlib"),
				new LibraryImport(0, "skybox/overlay_skybox.objlib"),
				new LibraryImport(0, "global/global_drones.objlib"),
				new LibraryImport(0, "global/global_tunnels.objlib"),
				new LibraryImport(0, "global/global_tunnels.objlib"),
				new LibraryImport(0, "global/global_tubes/global_tubes.objlib"),
				new LibraryImport(0, "global/global_caves/global_caves.objlib"),
				new LibraryImport(0, "global/global_lattices/global_lattices.objlib")
			));
		// @formatter:on
		return out.getBytes();
	}

	public static byte[] getObjDef0() {
		AuroraWriter out = new AuroraWriter();
		GfxLibImport s = new GfxLibImport();
		s.header = GfxLibImport.header();
		s.unknown0 = true;
		// @formatter:off
		s.groupings = List.of(
				new Grouping(List.of(new ParamPath(0xF04BF1D9, -1)), "kTraitObj", "skybox_src.mat"),
				new Grouping(List.of(new ParamPath(0xE92ABC92, -1)), "kTraitObj", "skybox_subtract.mat"),
				new Grouping(List.of(new ParamPath(0xC8FD3CD7, -1)), "kTraitBool", true)
			);
		s.comps = List.of(
				new PollComp(0),
				new EditStateComp(),
				new DrawComp(8, true, TraitLayer.kNumDrawLayers, TraitBucket.kBucketTerrain, List.of()),
				new XfmComp(1, "", TraitConstraint.kConstraintParent, Transform.identityScaled(150, 150, 150))
			);
		// @formatter:on
		out.obj(s);
		return out.getBytes();
	}

	public static byte[] getObjList1Bin() {
		AuroraWriter out = new AuroraWriter();
		// @formatter:off
		out.objlist(Arrays.asList(
				new LibraryObject(LibraryType.GfxLib, "skybox_cube", 0, "skybox/skybox_cube.objlib")
			));
		// @formatter:on
		return out.getBytes();
	}

	public static int getObjListCount() {
		return getDeclarations().size();
	}

	public static List<ObjectDeclaration> getDeclarations() {
		// @formatter:off
		return Arrays.asList(
				new ObjectDeclaration(DeclarationType.Mesh,          "web_points.mesh"), //1
				new ObjectDeclaration(DeclarationType.SequinDrawer,  "sequin.drawer"), // 1?
				new ObjectDeclaration(DeclarationType.TraitAnim,     "gamma_modulation.anim"),
				new ObjectDeclaration(DeclarationType.Mat,           "skybox_src.mat"), //1
				new ObjectDeclaration(DeclarationType.Mat,           "skybox_subtract.mat"), //1
				new ObjectDeclaration(DeclarationType.TraitAnim,     "skybox_rotation.anim"), //1
				new ObjectDeclaration(DeclarationType.Flow,          "level_start.flow"),
				new ObjectDeclaration(DeclarationType.Flow,          "beneath_ice.flow"), //1 //sfx
				new ObjectDeclaration(DeclarationType.Env,           "world.env"), // 1
				new ObjectDeclaration(DeclarationType.TraitAnim,     "skybox_colors.anim"),  // 1
				new ObjectDeclaration(DeclarationType.Flow,          "skybox_colors.flow"), // 1 skybox colors
				new ObjectDeclaration(DeclarationType.TraitAnim,     "boss_colors.anim"), //1
				new ObjectDeclaration(DeclarationType.Flow,          "boss_colors.flow"), //1 skybox colors
				new ObjectDeclaration(DeclarationType.TraitAnim,     "crakhed_colors.anim"), //1
				new ObjectDeclaration(DeclarationType.Flow,          "crakhed_colors.flow"), //1 skybox colors
				new ObjectDeclaration(DeclarationType.TraitAnim,     "level_colors.anim"), //1
				new ObjectDeclaration(DeclarationType.Flow,          "level_colors.flow"), //1 skybox colors
				new ObjectDeclaration(DeclarationType.Flow,          "level_colors_switch.flow"), //1
				new ObjectDeclaration(DeclarationType.Flow,          "turn_anticipation.flow"), //1 sfx
				new ObjectDeclaration(DeclarationType.TraitAnim,     "pyramid_colors.anim"), //1
				new ObjectDeclaration(DeclarationType.Flow,          "pyramid_colors.flow"), //1 skybox colors
				new ObjectDeclaration(DeclarationType.Flow,          "ending_sequence.flow"), //1 (level 9 ending)
				new ObjectDeclaration(DeclarationType.TraitAnim,     "diamond_colors.anim"), //1
				new ObjectDeclaration(DeclarationType.Flow,          "diamond_colors.flow"), //1 skybox colors
				new ObjectDeclaration(DeclarationType.TraitAnim,     "ending_sequence_colors.anim"), //1
				new ObjectDeclaration(DeclarationType.PathDecorator, "start_cap_tunnel_chrome.dec"), //1 probably needed
				new ObjectDeclaration(DeclarationType.Flow,          "fade.flow"), // 1
				new ObjectDeclaration(DeclarationType.Flow,          "ending_sequence_colors.flow"), //1
				new ObjectDeclaration(DeclarationType.Path,          "chrome_rail_20.path"),
				new ObjectDeclaration(DeclarationType.Path,          "chrome_rail_stretched.path"),
				new ObjectDeclaration(DeclarationType.Mat,           "level_arms.mat"), //1
				new ObjectDeclaration(DeclarationType.Mesh,          "level_arms_A.mesh"), //1
				new ObjectDeclaration(DeclarationType.Path,          "level_arms_A.path"), //1 tunnel
				new ObjectDeclaration(DeclarationType.Tex2D,         "gradient4.tex"), //1?
				new ObjectDeclaration(DeclarationType.Mat,           "tunnel_chrome.mat"), //1
				new ObjectDeclaration(DeclarationType.Mesh,          "tunnel_chrome.mesh"), //1
				new ObjectDeclaration(DeclarationType.Path,          "tunnel_chrome.path"), //1 tunnel
				new ObjectDeclaration(DeclarationType.Mat,           "tunnel_chrome_dark.mat"), //1
				new ObjectDeclaration(DeclarationType.Path,          "tunnel_chrome_dark.path"), //1 tunnel
				new ObjectDeclaration(DeclarationType.Mesh,          "tunnel_chrome_dark.mesh"), //1
				new ObjectDeclaration(DeclarationType.Path,          "landscape01.path"), //
				new ObjectDeclaration(DeclarationType.Mesh,          "landscape01.mesh"), //		these landscape tunnels are debug paths
				new ObjectDeclaration(DeclarationType.Tex2D,         "landscape1.tex"), //		we probably don't need these. they look terrible in game
				new ObjectDeclaration(DeclarationType.Mat,           "landscape.mat"), //
				new ObjectDeclaration(DeclarationType.Flow,          "extreme_path_sx.flow"), // this sounds interesting. should look into what this does
				new ObjectDeclaration(DeclarationType.SequinPulse,   "angle_frac_reverse.pulse"), //1
				new ObjectDeclaration(DeclarationType.SequinPulse,   "angle_frac.pulse"), //1 boss fx
				new ObjectDeclaration(DeclarationType.Tex2D,         "psychedelic10_BW.tex"), //1
				new ObjectDeclaration(DeclarationType.Mat,           "psychedelic10_BW.mat"), //1
				new ObjectDeclaration(DeclarationType.Mesh,          "occlusion_tube3_psych_BW.mesh"), //1
				new ObjectDeclaration(DeclarationType.Path,          "occlusion_tube03_psych_BW.path"), //1 tunnel
				new ObjectDeclaration(DeclarationType.TraitAnim,     "tex_scroll_level9.anim"), //1?
				new ObjectDeclaration(DeclarationType.TraitAnim,     "pyramid_invert.anim"), //1
				new ObjectDeclaration(DeclarationType.Mesh,          "lattice.mesh"), //1
				new ObjectDeclaration(DeclarationType.Path,          "lattice.path"), //1 tunnel
				new ObjectDeclaration(DeclarationType.Mat,           "lattice_chrome.mat"), //1
				new ObjectDeclaration(DeclarationType.Tex2D,         "gradient_lattice_chrome.tex"), //1
				new ObjectDeclaration(DeclarationType.Tex2D,         "lattice_emissive.tex"), //1
				new ObjectDeclaration(DeclarationType.Mesh,          "web.mesh"), //1
				new ObjectDeclaration(DeclarationType.Path,          "web.path"), //1 tunnel
				new ObjectDeclaration(DeclarationType.Path,          "web_points.path"), //1 tunnel
				new ObjectDeclaration(DeclarationType.Flow,          "boss_frac_pulse.flow") //1 boss fx
			);
		// @formatter:on
	}

	/**
	 * Generates the binary content of obj_list_2.objlib previously used by TML
	 * There *MUST* be getObjListCount() declarations and they *CANNOT* be moved or
	 * removed When the object declaration count is written, make sure to add
	 * getObjListCount() additional objects to account for this table if inserted
	 */
	public static byte[] getObjList2Bin() {
		// In the object declarations below, each end with a comment, 1 meaning we
		// *think* this is required for level operation, no comment if we think this can
		// be removed

		AuroraWriter out = new AuroraWriter();
		out.objlist(getDeclarations());

		// Remove first 4 bytes, tcle expects this bin dump without the list size
		out.removei8(0);
		out.removei8(0);
		out.removei8(0);
		out.removei8(0);

		return out.getBytes();
	}

	public static byte[] readBins() throws IOException {
		var decls = getDeclarations();

		AuroraWriter out = new AuroraWriter();

		for (var decl : decls) {
			out.i8arr(Util.getResourceBytes("bins/" + decl.name));
		}

		return out.getBytes();
	}
}
