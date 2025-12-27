package xyz.anthofoxo.aurora.struct;

import java.util.Arrays;

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

	public static byte[] getObjList1Bin() {
		AuroraWriter out = new AuroraWriter();
		// @formatter:off
		out.objlist(Arrays.asList(
				new LibraryObject(0x1BA51443, "skybox_cube", 0, "skybox/skybox_cube.objlib")
			));
		// @formatter:on
		return out.getBytes();
	}

	public static int getObjListCount() {
		return 62;
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
		// @formatter:off
		out.objlist(Arrays.asList(
				new ObjectDeclaration(DeclarationType.Mesh,          "web_points.mesh"),
				new ObjectDeclaration(DeclarationType.SequinDrawer,  "sequin.drawer"), // 1?
				new ObjectDeclaration(DeclarationType.TraitAnim,     "gamma_modulation.anim"),
				new ObjectDeclaration(DeclarationType.Mat,           "skybox_src.mat"),
				new ObjectDeclaration(DeclarationType.Mat,           "skybox_subtract.mat"),
				new ObjectDeclaration(DeclarationType.TraitAnim,     "skybox_rotation.anim"),
				new ObjectDeclaration(DeclarationType.Flow,          "level_start.flow"),
				new ObjectDeclaration(DeclarationType.Flow,          "beneath_ice.flow"),
				new ObjectDeclaration(DeclarationType.Env,           "world.env"), // 1
				new ObjectDeclaration(DeclarationType.TraitAnim,     "skybox_colors.anim"),  // 1
				new ObjectDeclaration(DeclarationType.Flow,          "skybox_colors.flow"), // 1
				new ObjectDeclaration(DeclarationType.TraitAnim,     "boss_colors.anim"),
				new ObjectDeclaration(DeclarationType.Flow,          "boss_colors.flow"),
				new ObjectDeclaration(DeclarationType.TraitAnim,     "crakhed_colors.anim"),
				new ObjectDeclaration(DeclarationType.Flow,          "crakhed_colors.flow"),
				new ObjectDeclaration(DeclarationType.TraitAnim,     "level_colors.anim"),
				new ObjectDeclaration(DeclarationType.Flow,          "level_colors.flow"),
				new ObjectDeclaration(DeclarationType.Flow,          "level_colors_switch.flow"),
				new ObjectDeclaration(DeclarationType.Flow,          "turn_anticipation.flow"),
				new ObjectDeclaration(DeclarationType.TraitAnim,     "pyramid_colors.anim"),
				new ObjectDeclaration(DeclarationType.Flow,          "pyramid_colors.flow"),
				new ObjectDeclaration(DeclarationType.Flow,          "ending_sequence.flow"),
				new ObjectDeclaration(DeclarationType.TraitAnim,     "diamond_colors.anim"),
				new ObjectDeclaration(DeclarationType.Flow,          "diamond_colors.flow"),
				new ObjectDeclaration(DeclarationType.TraitAnim,     "ending_sequence_colors.anim"),
				new ObjectDeclaration(DeclarationType.PathDecorator, "start_cap_tunnel_chrome.dec"),
				new ObjectDeclaration(DeclarationType.Flow,          "fade.flow"), // 1
				new ObjectDeclaration(DeclarationType.Flow,          "ending_sequence_colors.flow"),
				new ObjectDeclaration(DeclarationType.Path,          "chrome_rail_20.path"),
				new ObjectDeclaration(DeclarationType.Path,          "chrome_rail_stretched.path"),
				new ObjectDeclaration(DeclarationType.Mat,           "level_arms.mat"),
				new ObjectDeclaration(DeclarationType.Mesh,          "level_arms_A.mesh"),
				new ObjectDeclaration(DeclarationType.Path,          "level_arms_A.path"),
				new ObjectDeclaration(DeclarationType.Tex2D,         "gradient4.tex"),
				new ObjectDeclaration(DeclarationType.Mat,           "tunnel_chrome.mat"),
				new ObjectDeclaration(DeclarationType.Mesh,          "tunnel_chrome.mesh"),
				new ObjectDeclaration(DeclarationType.Path,          "tunnel_chrome.path"),
				new ObjectDeclaration(DeclarationType.Mat,           "tunnel_chrome_dark.mat"),
				new ObjectDeclaration(DeclarationType.Path,          "tunnel_chrome_dark.path"),
				new ObjectDeclaration(DeclarationType.Mesh,          "tunnel_chrome_dark.mesh"),
				new ObjectDeclaration(DeclarationType.Path,          "landscape01.path"), // 1
				new ObjectDeclaration(DeclarationType.Mesh,          "landscape01.mesh"), // 1
				new ObjectDeclaration(DeclarationType.Tex2D,         "landscape1.tex"), // 1
				new ObjectDeclaration(DeclarationType.Mat,           "landscape.mat"), // 1
				new ObjectDeclaration(DeclarationType.Flow,          "extreme_path_sx.flow"),
				new ObjectDeclaration(DeclarationType.SequinPulse,   "angle_frac_reverse.pulse"),
				new ObjectDeclaration(DeclarationType.SequinPulse,   "angle_frac.pulse"),
				new ObjectDeclaration(DeclarationType.Tex2D,         "psychedelic10_BW.tex"),
				new ObjectDeclaration(DeclarationType.Mat,           "psychedelic10_BW.mat"),
				new ObjectDeclaration(DeclarationType.Mesh,          "occlusion_tube3_psych_BW.mesh"),
				new ObjectDeclaration(DeclarationType.Path,          "occlusion_tube03_psych_BW.path"),
				new ObjectDeclaration(DeclarationType.TraitAnim,     "tex_scroll_level9.anim"),
				new ObjectDeclaration(DeclarationType.TraitAnim,     "pyramid_invert.anim"),
				new ObjectDeclaration(DeclarationType.Mesh,          "lattice.mesh"),
				new ObjectDeclaration(DeclarationType.Path,          "lattice.path"),
				new ObjectDeclaration(DeclarationType.Mat,           "lattice_chrome.mat"),
				new ObjectDeclaration(DeclarationType.Tex2D,         "gradient_lattice_chrome.tex"),
				new ObjectDeclaration(DeclarationType.Tex2D,         "lattice_emissive.tex"),
				new ObjectDeclaration(DeclarationType.Mesh,          "web.mesh"),
				new ObjectDeclaration(DeclarationType.Path,          "web.path"),
				new ObjectDeclaration(DeclarationType.Path,          "web_points.path"),
				new ObjectDeclaration(DeclarationType.Flow,          "boss_frac_pulse.flow"))
			);
		// @formatter:on

		// Remove first 4 bytes, tcle expects this bin dump without the list size
		out.removei8(0);
		out.removei8(0);
		out.removei8(0);
		out.removei8(0);

		return out.getBytes();
	}
}
