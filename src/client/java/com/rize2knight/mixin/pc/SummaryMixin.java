package com.rize2knight.mixin.pc;

import com.rize2knight.GUIHandler;
import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.client.gui.ExitButton;
import com.cobblemon.mod.common.client.gui.summary.Summary;
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.SummaryTab;
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.stats.StatWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Summary.class)
public abstract class SummaryMixin extends Screen {

    @Shadow(remap = false) @Final public static int BASE_WIDTH;
    @Shadow(remap = false) @Final public static int BASE_HEIGHT;
    @Shadow(remap = false) @Final private List<SummaryTab> summaryTabs;

    protected SummaryMixin(Component component) {
        super(component);
    }

    @Shadow public abstract void playSound(@NotNull SoundEvent soundEvent);

    @Shadow(remap = false) private int mainScreenIndex;

    @Shadow private AbstractWidget mainScreen;

    @Inject(method = "onClose", at = @At("TAIL"))
    private void cobblemon_ui_tweaks$onClose(CallbackInfo ci) {
        GUIHandler.INSTANCE.onSummaryClose();
    }

    @Redirect(
            method = "init",
            at = @At(
                    value = "NEW",
                    target = "(IILnet/minecraft/client/gui/components/Button$OnPress;)Lcom/cobblemon/mod/common/client/gui/ExitButton;"
            )
    )
    private ExitButton cobblemon_ui_tweaks$init(int pX, int pY, Button.OnPress onPress) {
        var x = (width - BASE_WIDTH) / 2;
        var y = (height - BASE_HEIGHT) / 2;

        return new ExitButton(x + 302, y + 145, (button) -> {
            playSound(CobblemonSounds.GUI_CLICK);
            Minecraft.getInstance().setScreen(null);
            GUIHandler.INSTANCE.onSummaryClose();
        });
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void cobblemon_ui_tweaks$initHead(CallbackInfo ci) {
        if (GUIHandler.INSTANCE.getPC() != null) {
            this.mainScreenIndex = GUIHandler.INSTANCE.getLastSummaryTabIndex();
        }
    }

    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    private void cobblemon_ui_tweaks$toggleTab(CallbackInfo ci) {
        if (GUIHandler.INSTANCE.getPC() != null) {
            summaryTabs.forEach(tab -> tab.toggleTab(false));
            summaryTabs.get(GUIHandler.INSTANCE.getLastSummaryTabIndex()).toggleTab(true);
        }
    }

    @Inject(method = "displayMainScreen", at = @At("HEAD"), remap = false)
    private void cobblemon_ui_tweaks$displayMainScreenHead(CallbackInfo ci) {
        if (mainScreen == null) {
            this.mainScreenIndex = 0;
        }
    }

    @Inject(method = "displayMainScreen", at = @At("TAIL"), remap = false)
    private void cobblemon_ui_tweaks$displayMainScreen(CallbackInfo ci) {
        if (GUIHandler.INSTANCE.getPC() != null) {
            if (this.mainScreen instanceof StatWidget statWidget) {
                statWidget.setStatTabIndex(GUIHandler.INSTANCE.getLastStatsTabIndex());
            }

            GUIHandler.INSTANCE.setLastSummaryTabIndex(this.mainScreenIndex);
        }
    }

}
