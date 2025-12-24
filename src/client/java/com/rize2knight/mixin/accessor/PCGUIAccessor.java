package com.rize2knight.mixin.accessor;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = PCGUI.class, priority = 1001)
public interface PCGUIAccessor{
    @Accessor(value = "currentStatIndex", remap = false)
    int getCurrentStatIndex();
}
