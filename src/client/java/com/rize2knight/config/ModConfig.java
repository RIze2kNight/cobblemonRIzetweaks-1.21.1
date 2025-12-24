package com.rize2knight.config;

import com.rize2knight.CobblemonRizeTweaksClient;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = CobblemonRizeTweaksClient.MODID)
public class ModConfig implements ConfigData {
    @ConfigEntry.Category("RIzeTweaks")
    public boolean pcBoxJump = true;

    @ConfigEntry.Category("RIzeTweaks")
    public boolean HAHightlighter = true;

    @ConfigEntry.Category("RIzeTweaks")
    public boolean typeChanges = true;

    @ConfigEntry.Category("RIzeTweaks") @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @Comment("""
                ALL: Renders RIzeTweaks Move Helper for Single and Doubles.
                RIzeMultiBattle: Only renders RIzeTweaks Move Helper during multi-battles.
                DISABLE: Disable all Move Helpers.
                """)
    public BattleGUIRendererStyle battleGUIStyle = BattleGUIRendererStyle.ALL;
}
