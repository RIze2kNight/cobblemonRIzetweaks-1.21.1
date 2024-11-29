package com.rize2knight.mixin.pc;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.rize2knight.GUIHandler;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.keybind.CobblemonKeyBinds;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.mixin.accessor.KeyBindingAccessor;
import com.rize2knight.JumpPCBoxWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import static com.cobblemon.mod.common.client.gui.pc.PCGUI.*;

@Mixin(PCGUI.class)
public abstract class PCGUIMixin extends Screen {

    @Unique private static final Logger LOGGER = LoggerFactory.getLogger("cobblemonuitweaks");
    @Shadow(remap = false) private StorageWidget storageWidget;
    @Final @Shadow(remap = false) private ClientPC pc;
    @Shadow public abstract void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta);

    @Shadow private Pokemon previewPokemon = null;
    @Unique private JumpPCBoxWidget jumpPCBoxWidget;        // Add a reference to the JumpPCBoxWidget to manage focus

    protected PCGUIMixin(Component component) {
        super(component);
        LOGGER.info("Initializing PCGUIMixin with storageWidget: {}", storageWidget);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void cobblemon_ui_tweaks$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        var summaryKey = (KeyBindingAccessor) CobblemonKeyBinds.INSTANCE.getSUMMARY();
        if (keyCode == summaryKey.boundKey().getValue()) {
            GUIHandler.INSTANCE.onSummaryPressFromPC((PCGUI)(Object)this);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        var pastureWidget = storageWidget.getPastureWidget();
        if (pastureWidget != null && pastureWidget.getPastureScrollList().isMouseOver(mouseX, mouseY)) {
            pastureWidget.getPastureScrollList().mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        else {
            var newBox = (storageWidget.getBox() - (int)verticalAmount) % this.pc.getBoxes().size();
            storageWidget.setBox(newBox);
        }

        // If JumpPCBoxWidget is set, unfocus the EditBox to prevent de-synced box numbers
        if (jumpPCBoxWidget != null) {
            jumpPCBoxWidget.setFocused(false);
        }


        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Inject(method = "closeNormally", at = @At("TAIL"), remap = false)
    private void cobblemon_ui_tweaks$closeNormally(CallbackInfo ci) {
        GUIHandler.INSTANCE.onPCClose();
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lcom/cobblemon/mod/common/client/gui/pc/PCGUI;playSound(Lnet/minecraft/sounds/SoundEvent;)V"))
    private void cobblemon_ui_tweaks$keyPressedToClosePC(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        GUIHandler.INSTANCE.onPCClose();
    }

    //Stop render original PC Box Title and replace with JumpPCBox/rest of code init
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/cobblemon/mod/common/client/render/RenderHelperKt;drawScaledText$default(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/chat/MutableComponent;Ljava/lang/Number;Ljava/lang/Number;FLjava/lang/Number;IIZZLjava/lang/Integer;Ljava/lang/Integer;ILjava/lang/Object;)V",
                    ordinal = 12// Target the 13th/Box Label occurrence (0-based index)
            ),
            cancellable = true               // Allow cancellation if necessary
    )
    private void stopOGPartyTitleRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {

        var pokemon = previewPokemon;
        var x = (width - BASE_WIDTH) / 2;
        var y = (height - BASE_HEIGHT) / 2;
        var PCBox = storageWidget.getBox() + 1;

        int newX = ((width - BASE_WIDTH) / 2) + 140;
        int newY = ((height - BASE_HEIGHT) / 2) + 15;

        if(jumpPCBoxWidget == null || jumpPCBoxWidget.getX() != newX || jumpPCBoxWidget.getY() != newY){        // (Re)Initialises JumpPCBoxWidget when screen resizes
            jumpPCBoxWidget = new JumpPCBoxWidget(
                    storageWidget,
                    pc,
                    x + 140,
                    y + 15,
                    60,
                    PCGUI.PC_SPACER_HEIGHT,
                    Component.translatable("cobblemon.ui.pc.box.title", Component.literal(String.valueOf(PCBox)).withStyle(ChatFormatting.BOLD))
            );

            this.addRenderableWidget(jumpPCBoxWidget);
        }

        super.render(context, mouseX, mouseY, delta);

        /// Item Tooltip
        if (pokemon != null && !pokemon.getHeldItem$common().isEmpty()) {
            int itemX = x + 3;
            int itemY = y + 98;
            boolean itemHovered =
                    (mouseX >= itemX && mouseX <= (itemX + 16)) && (mouseY >= itemY && mouseY <= (itemY + 16));
            if (itemHovered) {
                context.renderTooltip(
                        Minecraft.getInstance().font,
                        pokemon.heldItemNoCopy$common(),
                        mouseX,
                        mouseY
                );
            }
        }

        ci.cancel();
    }
}
