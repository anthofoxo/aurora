local hashes = {
	-- Objlib Object Declaration Type Names
	"SequinLeaf",
	"Sample",
	"EntitySpawner",
	"SequinMaster",
	"SequinDrawer",
	"SequinGate",
	"SequinLevel",
	"Path",
	"Mesh",
	"Mat",
	"Flow",
	"TraitAnim",
	"Cam",
	"Env",
	"PathDecorator",
	"Tex2D",
	"Tex3D",
	"Xfmer",
	"ChannelGroup",
	"DSP",
	"Bender",

	-- Localization Keys
	"accept",
	"no",
	"level1",
	"level2",
	"level3",
	"level4",
	"level5",
	"level6",
	"level7",
	"level8",
	"level9",
	"cancel",
	"continue",
	"play",
	"yes",
	"leaderboard_view",
	"rank",
	"retry",
    "tip",

	-- Undocumented
	"AnimComp",
	"EditStateComp",
    "XfmComp",
    "intensity",
    "local_rot",
    "world.cam",
    "camera_shake.anim",
    "death_cam_shake.anim",

	-- object parameters --

	-- .lvl objects
	"layer_volume",
	-- .leaf objects
	"pitch",
	"roll",
	"turn",
	"turn_auto",
	"scale_x",
	"scale_y",
	"scale_z",
	"offset_x",
	"offset_y",
	"offset_z",
	"visibla01",
	"visibla02",
	"visible",
	"visiblz01",
	"visiblz02",
	-- avatar .objlib objects
	"sequin_speed",
	"win",
	"win_checkpoint",
	"win_checkpoint_silent",
	-- .samp objects
	"play",
	"play_clean",
	"pause",
	"resume",
	"stop",
	-- .mat objects
	"emissive_color",
	"ambient_color",
	"diffuse_color",
	"specular_color",
	"reflectivity_color",
	"alpha",
	-- .anim objects
	"frame",

	-- interactive player objects --

	-- .spn objects: decorators/thump_rails.objlib
	"thump_rails.a01",
	"thump_rails.a02",
	"thump_rails.ent",
	"thump_rails.z01",
	"thump_rails.z02",
	"thump_checkpoint.ent",
	"thump_rails_fast_activat.ent",
	"thump_boss_bonus.ent",
	-- .spn objects: decorators/thump_grindable.objlib
	"grindable_still.ent",
	"left_multi.a01",
	"left_multi.a02",
	"left_multi.ent",
	"left_multi.z01",
	"center_multi.a02",
	"center_multi.ent",
	"center_multi.z01",
	"right_multi.a02",
	"right_multi.ent",
	"right_multi.z01",
	"right_multi.z02",
	-- .spn objects : decorators / thump_grindable_multi.objlib
	"grindable_quarters.ent",
	"grindable_double.ent",
	"grindable_thirds.ent",
	"grindable_with_thump.ent",
	-- 	.spn objects: decorators/ducker.objlib
	"ducker_crak.ent",
	-- .spn objects : decorators / jumper / jumper_set.objlib
	"jumper_1_step.ent",
	"jumper_boss.ent",
	"jumper_6_step.ent",
	-- .spn objects: decorators/jump_high/jump_high_set.objlib
	"jump_high.ent",
	"jump_high_2.ent",
	"jump_high_4.ent",
	"jump_high_6.ent",
	"jump_boss.ent",
	-- .spn objects : decorators / obstacles / wurms / millipede_half.objlib
	"swerve_off.a01",
	"swerve_off.a02",
	"swerve_off.ent",
	"swerve_off.z01",
	"swerve_off.z02",
	"millipede_half.a01",
	"millipede_half.a02",
	"millipede_half.ent",
	"millipede_half.z01",
	"millipede_half.z02",
	"millipede_half_phrase.a01",
	"millipede_half_phrase.a02",
	"millipede_half_phrase.ent",
	"millipede_half_phrase.z01",
	"millipede_half_phrase.z02",
	-- .spn objects: decorators/obstacles/wurms/millipede_quarter.objlib
	"millipede_quarter.a01",
	"millipede_quarter.a02",
	"millipede_quarter.ent",
	"millipede_quarter.z01",
	"millipede_quarter.z02",
	"millipede_quarter_phrase.a01",
	"millipede_quarter_phrase.a02",
	"millipede_quarter_phrase.ent",
	"millipede_quarter_phrase.z01",
	"millipede_quarter_phrase.z02",
	-- .spn objects: decorators/sentry.objlib
	"sentry.ent",
	"level_9.ent",
	"level_5.ent",
	"level_8.ent",
	"sentry_boss.ent",
	"level_7.ent",
	"level_6.ent",
	"sentry_boss_multilane.ent",
	"level_8_multi.ent",
	"level_9_multi.ent",

	-- decorative objects --

	-- .spn objects: decorators/jump_high/jump_high_big_trees_set.objlib
	"trees.ent",
	"trees_16.ent",
	"trees_4.ent",
	-- .spn objects: entity/ambient_fx.objlib
	"speed_streaks_short.ent",
	"speed_streaks_RGB.ent",
	"smoke.ent",
	"death_shatter.ent",
	"speed_streaks.ent",
	"data_streaks_radial.ent",
	"boss_7_tunnel_enter.ent",
	"boss_damage_stage4.ent",
	"crakhed_damage.ent",
	"win_debris.ent",
	"crakhed_destroy.ent",
	"stalactites.ent",
	"aurora.ent",
	"vortex_decorator.ent",
	"boss_damage_stage3.ent",
	"boss_damage_stage1.ent",
	"boss_damage_stage2.ent",
	-- skybox_colors.flow (levels/demo.objlib)
	"black",
	"crakhed",
	"dark_blue",
	"dark_green",
	"dark_red",
	"light_blue",
	"light_green",
	"light_red",

	-- SFX objects --

	-- 	turn_anticipation.flow (levels/Level6/level_6.objlib)
	"fire",
	-- dissonant_bursts.flow (global/dissonant_bursts.objlib)
	"diss11",
	-- french_horn_chords.flow (global/french_horn_chords.objlib)
	"french12",

	-- --- Bosses (.gate objects only) ---

	-- 	.spn objects: boss/gate_triangle/triangle_boss.objlib
	"tutorial_thumps.ent",
	-- .spn objects: boss/boss_spiral/gate_spiral.objlib
	"boss_gate_pellet.ent",
}
	
for _, value in ipairs(dofile("aurora/hashtable/pc_list.lua")) do table.insert(hashes, value) end

if Aurora.filesystem.exists("aurora/hashtable/userdef.lua") then
	for _, value in ipairs(dofile("aurora/hashtable/userdef.lua")) do table.insert(hashes, value) end
end

local hashtable = {}

for _, value in ipairs(hashes) do
	hashtable[Aurora.hash(value)] = value
end

return hashtable
