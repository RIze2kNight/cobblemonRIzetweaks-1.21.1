package com.rize2knight.mixin.pc;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.client.gui.ExitButton;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.summary.Summary;
import com.rize2knight.CobblemonRizeTweaksClient;
import com.rize2knight.config.ModConfig;
import com.rize2knight.mixin.accessor.MovesWidgetAccessor;
import com.rize2knight.util.RIzeTweaksUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.cobblemon.mod.common.client.gui.summary.Summary.BASE_HEIGHT;
import static com.cobblemon.mod.common.client.gui.summary.Summary.BASE_WIDTH;

@Mixin(value = Summary.class, priority = 1001)
public abstract class SummaryMixin extends Screen {
    @Unique private static ModConfig config = CobblemonRizeTweaksClient.INSTANCE.getConfig();

    @Shadow(remap = false) protected abstract void displayMainScreen(int screen);
    @Shadow(remap = false) public abstract void playSound(SoundEvent soundEvent);
    @Shadow(remap = false) private int mainScreenIndex;
    @Shadow(remap = false) @Final private static int MARKS;
    @Shadow(remap = false) @Final private static int INFO;
    @Shadow private AbstractWidget mainScreen;

    protected SummaryMixin(Component component) {
        super(component);
    }

    @Inject(method = "onClose", at = @At(value = "RETURN"))
    private void overrideOnClose(CallbackInfo ci){
        //When Summary closes and Summary was opened from PC, reopen PCGUI
        closeSummaryOpenPC();
    }

    @Redirect(
            method = "init",
            at = @At(
                    value = "NEW",
                    target = "(IILnet/minecraft/client/gui/components/Button$OnPress;)Lcom/cobblemon/mod/common/client/gui/ExitButton;"
            )
    )
    private ExitButton redirectExitButton(
            int pX,
            int pY,
            Button.OnPress onPress)
    {
        var x = (width - BASE_WIDTH) / 2;
        var y = (height - BASE_HEIGHT) / 2;

        //Replaces Summary ExitButton with reopen PCGUI logic if Summary was opened from PC
        return new ExitButton(x + 302, y + 145, (button) -> {
            playSound(CobblemonSounds.GUI_CLICK);
            Minecraft.getInstance().setScreen(null);

            closeSummaryOpenPC();
        });
    }

    @Inject(method = "mouseDragged", at = @At(value = "HEAD"))
    private void overrideMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir){
        if(mainScreenIndex == INFO && RIzeTweaksUtil.abilityLabelK != null){
            RIzeTweaksUtil.abilityLabelK.mouseDragged(button,deltaY);
        }
    }

    @Inject(method = "mouseScrolled", at = @At(value = "HEAD"), cancellable = true)
    private void overrideMouseScrolled(double mouseX, double mouseY, double amount, double verticalAmount, CallbackInfoReturnable<Boolean> cir){
        if(mainScreen == null || !config.summaryTabScroll)return;
        if (!mainScreen.isHovered()) return;

        if (mainScreen instanceof MovesWidgetAccessor mw &&
            mw.getDescriptionScrollList().isHovered()) return;

        if (RIzeTweaksUtil.abilityLabelK != null &&
            RIzeTweaksUtil.abilityLabelK.isHovered() &&
            RIzeTweaksUtil.abilityLabelK.hasOverflow()) {
                RIzeTweaksUtil.abilityLabelK.mouseScrolled(verticalAmount);
                return;
        }

        // Scroll between Summary Tabs
        mainScreenIndex = Math.floorMod(mainScreenIndex - (int) Math.signum(verticalAmount), MARKS + 1);
        displayMainScreen(mainScreenIndex);

        cir.setReturnValue(true);
        cir.cancel();
    }

    // Close Summary and reopen PCGUI if Summary was opened from PC
    @Unique
    private void closeSummaryOpenPC(){
        if(RIzeTweaksUtil.summaryOpenedFromPC && RIzeTweaksUtil.pcGUI != null){
            PCGUI pc = RIzeTweaksUtil.pcGUI;
            Minecraft.getInstance().setScreen(pc);

            RIzeTweaksUtil.summaryOpenedFromPC = false;
            RIzeTweaksUtil.pcGUI = null;
        }
    }
}
