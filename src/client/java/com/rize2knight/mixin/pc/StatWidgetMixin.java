package com.rize2knight.mixin.pc;

import com.rize2knight.GUIHandler;
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.stats.StatWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatWidget.class)
public class StatWidgetMixin {

    @Shadow(remap = false) private int statTabIndex;

    @Inject(method = "mouseClicked", at = @At("TAIL"))
    private void cobblemon_ui_tweaks$mouseClicked(double pMouseX, double pMouseY, int pButton, CallbackInfoReturnable<Boolean> cir) {
        if (GUIHandler.INSTANCE.getPC() != null) {
            GUIHandler.INSTANCE.setLastStatsTabIndex(this.statTabIndex);
        }
    }

}
