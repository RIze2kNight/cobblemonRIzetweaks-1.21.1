package com.rize2knight.config;

import com.rize2knight.CobblemonRizeTweaksClient;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.BooleanToggleBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ConfigScreen {
    public static Screen buildScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("CobblemonRizeTweaks"));

        final ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        final ConfigCategory rizeTweaks = builder.getOrCreateCategory(Component.literal("Rize's Tweaks"));
        final ConfigCategory fixes = builder.getOrCreateCategory(Component.literal("Fixes"));

        /* Rize Config Options */
        rizeTweaks.addEntry(basicToggle(entryBuilder,"pc_box_jump"));
        rizeTweaks.addEntry(basicToggle(entryBuilder,"hidden_ability_highlighter"));
        rizeTweaks.addEntry(basicToggle(entryBuilder,"move_tips"));
        rizeTweaks.addEntry(basicToggle(entryBuilder,"type_changes"));

        /* Fixes Config Options */
        fixes.addEntry(basicToggle(entryBuilder,"cobblemonuitweaks_pc_scroll_fix"));
        fixes.addEntry(basicToggle(entryBuilder,"cobblemonuitweaks_last_pc_box_fix"));

        builder.setSavingRunnable(ModConfig::saveConfig);
        return builder.build();
    }

    private static AbstractConfigListEntry<?> basicToggle(ConfigEntryBuilder builder, String key) {
        return basicToggle(builder, key, entry -> {});
    }

    private static AbstractConfigListEntry<?> basicToggle(ConfigEntryBuilder builder, String key, Consumer<BooleanToggleBuilder> unique) {
        BooleanToggleBuilder entry = builder.startBooleanToggle(Component.translatable( CobblemonRizeTweaksClient.MODID + ".config." + key), ModConfig.getInstance().isEnabled(key))
                .setDefaultValue(true)
                .setSaveConsumer(value -> ModConfig.getInstance().setEnabled(key,value));
        unique.accept(entry);
        return entry.build();
    }
}
