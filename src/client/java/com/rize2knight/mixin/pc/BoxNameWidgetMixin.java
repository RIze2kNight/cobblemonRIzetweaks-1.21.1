package com.rize2knight.mixin.pc;

import com.cobblemon.mod.common.client.gui.pc.BoxNameWidget;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.gui.pc.TextWidget;
import com.mojang.blaze3d.platform.InputConstants;
import com.rize2knight.CobblemonRizeTweaksClient;
import com.rize2knight.config.ModConfig;
import com.rize2knight.keybind.keybinds.JumpPCBoxKey;
import com.rize2knight.util.RIzeTweaksUtil;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BoxNameWidget.class, priority = 1001)
public abstract class BoxNameWidgetMixin extends TextWidget {
    @Shadow public abstract void setFocused(boolean focused);
    @Final @Shadow(remap = false) private StorageWidget storageWidget;
    @Final @Shadow(remap = false) private PCGUI pcGui;

    @Unique private static ModConfig config = CobblemonRizeTweaksClient.INSTANCE.getConfig();

    public BoxNameWidgetMixin(int pX, int pY, int width, int height, int maxLength, @NotNull Component text, @NotNull Function0<Unit> update) {
        super(pX, pY, width, height, maxLength, text, update);
        CobblemonRizeTweaksClient.LOGGER.info("Initializing BoxNameWidgetMixin");
    }

    @ModifyArg(
            method = "renderWidget",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/cobblemon/mod/common/client/render/RenderHelperKt;drawScaledText$default(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/chat/MutableComponent;Ljava/lang/Number;Ljava/lang/Number;FLjava/lang/Number;IIZZLjava/lang/Integer;Ljava/lang/Integer;ILjava/lang/Object;)V"
            ),
            index = 2
    )
    private MutableComponent modifyBoxNameWidgetText(MutableComponent originalBoxName){
        //Conditional Render of PC Box Jump text when keybind pressed
        if(config.pcBoxJump && RIzeTweaksUtil.enablePCBoxJump && isFocused()) {
            return Component.translatable(
                    "cobblemon.ui.pc.box.title",
                    this.getValue() + "|"
            ).withStyle(style -> style.withBold(true));
        }

        return originalBoxName;
    }

    @Inject(method = "keyPressed", at = @At(value = "HEAD"), cancellable = true)
    private void overrideKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir){
        if(!config.pcBoxJump) return;

        //Goes to specific box upon pressing enter
        if (keyCode == InputConstants.KEY_RETURN && RIzeTweaksUtil.enablePCBoxJump){
            updateNewPCBox();

            //Unfocuses and resets to default box name behaviour
            setFocused(false);
            RIzeTweaksUtil.enablePCBoxJump = false;
            setValue("");

            cir.setReturnValue(true);
            cir.cancel();
        }

        //Unfocuses and resets to default box name behaviour upon pressing escape
        if(isFocused() && keyCode == InputConstants.KEY_ESCAPE){
            setValue("");
            setFocused(false);
            RIzeTweaksUtil.enablePCBoxJump = false;
        }
    }

    @Inject(method = "setFocused", at = @At(value = "HEAD"), cancellable = true)
    private void overrideSetFocused(boolean focused, CallbackInfo ci){
        if(!config.pcBoxJump) return;

        //Checks if PC Box Jump keybind is held down
        var window = Minecraft.getInstance().getWindow();
        var boundKey = KeyBindingHelper.getBoundKeyOf(JumpPCBoxKey.INSTANCE.mapping);
        var isJumpPCBoxKeyHeld = InputConstants.isKeyDown(window.getWindow(), boundKey.getValue());

        //Focuses Box Name Widget in PCBoxJump mode
        if(isJumpPCBoxKeyHeld && focused){
            RIzeTweaksUtil.enablePCBoxJump = true;
            setValue("");

            super.setFocused(true);
            ci.cancel();
        }
    }

    // When unfocused normally happens: if jump mode active, prevent original rename/unfocused logic
    @Inject(method = "unfocused", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private void overrideUnfocused(CallbackInfo ci){
        if(!config.pcBoxJump) return;

        //Resets to default box name behaviour (Maybe?)
        if (RIzeTweaksUtil.enablePCBoxJump) {
            setValue("");
            RIzeTweaksUtil.enablePCBoxJump = false;
            // Cancel original unfocused so it doesn't send rename packet / clear value again
            ci.cancel();
        }
    }

    //Function to go to specific box using box index (0-index) if input within PC Box size
    @Unique
    private void updateNewPCBox() {
        Integer newPCBox = null;
        try{ newPCBox = Integer.parseInt(this.getValue()); }
        catch(NumberFormatException ignore) {}

        if (newPCBox != null
                && newPCBox > 0
                && newPCBox <= pcGui.getPc().getBoxes().size()
                && newPCBox - 1 != storageWidget.getBox())
        {
            storageWidget.setBox(newPCBox - 1); // Update storage widget
        }
    }
}
