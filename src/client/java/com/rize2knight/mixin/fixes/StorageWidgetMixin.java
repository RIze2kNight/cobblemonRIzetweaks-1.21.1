package com.rize2knight.mixin.fixes;

import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.rize2knight.GUIHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = StorageWidget.class, priority = 1001)
public abstract class StorageWidgetMixin {
    @Shadow(remap = false) public abstract void setBox(int value);

    @Inject(method = "setBox", at = @At("TAIL"), remap = false)
    public void cobblemon_ui_tweaks$setBox(int value, CallbackInfo ci) {
        if (value != 0){ GUIHandler.INSTANCE.setLastPCBox(value); }
    }
}
