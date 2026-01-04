package xyz.anthofoxo.aurora;

import imgui.type.ImBoolean;

public class BuiltinModOptions {

	public static final String USERCONFIG_EQ_MOD_STR = "aurora.mod.eq.enabled";
	public static ImBoolean applyEqMod = new ImBoolean(
			Boolean.parseBoolean(UserConfig.get(USERCONFIG_EQ_MOD_STR, Boolean.toString(false))));

}
