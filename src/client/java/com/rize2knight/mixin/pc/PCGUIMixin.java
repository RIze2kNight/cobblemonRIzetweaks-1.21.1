package com.rize2knight.mixin.pc;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.rize2knight.CobblemonRizeTweaksClient;
import com.rize2knight.HAHighlighterRenderer;
import com.rize2knight.JumpPCBoxWidget;
import com.rize2knight.config.ModConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static com.cobblemon.mod.common.client.gui.pc.PCGUI.*;

@Mixin(value = PCGUI.class, priority = 1001)
public abstract class PCGUIMixin extends Screen {
    @Unique private static ModConfig config = CobblemonRizeTweaksClient.INSTANCE.getConfig();
    @Shadow(remap = false) private StorageWidget storageWidget;
    @Final @Shadow(remap = false) private ClientPC pc;
    @Shadow public abstract void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta);
    @Shadow(remap = false) private Pokemon previewPokemon = null;

    @Unique private JumpPCBoxWidget jumpPCBoxWidget;        // Add a reference to the JumpPCBoxWidget to manage focus
    @Unique private static final Logger LOGGER = CobblemonRizeTweaksClient.INSTANCE.getLOGGER();

    protected PCGUIMixin(Component component) {
        super(component);
        LOGGER.info("Initializing PCGUIMixin with storageWidget: {}", storageWidget);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (FabricLoader.getInstance().isModLoaded("cobblemon-ui-tweaks")
            && config.uiTweaks_pcScrollFix){
            var pastureWidget = storageWidget.getPastureWidget();
            if (pastureWidget != null && pastureWidget.getPastureScrollList().isMouseOver(mouseX, mouseY)) {
                pastureWidget.getPastureScrollList().mouseScrolled(mouseX, mouseY, horizontalAmount,verticalAmount);
            }
            else {
                var newBox = (storageWidget.getBox() - (int)verticalAmount) % this.pc.getBoxes().size();
                storageWidget.setBox(newBox);
            }
        }

        // If JumpPCBoxWidget is set, unfocus the EditBox to prevent de-synced box numbers
        if(jumpPCBoxWidget != null && config.pcBoxJump) {
            jumpPCBoxWidget.setFocused(false);
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    //Renders the PC Box Jump widget
    @Inject(method = "init", at = @At(value = "TAIL"))
    private void cobblemon_rize_tweaks$init(CallbackInfo ci) {
        LOGGER.info("CobblemonRIzeTweaks PCGUIMixin @inject init initialising");

        if(config.pcBoxJump) {
            var PCBox = storageWidget.getBox() + 1;
            jumpPCBoxWidget = new JumpPCBoxWidget(
                    this.storageWidget,
                    this.pc,
                    ((width - BASE_WIDTH) / 2) + 140,
                    ((height - BASE_HEIGHT) / 2) + 15,
                    60,
                    PC_SPACER_HEIGHT,
                    Component.translatable("cobblemon.ui.pc.box.title", Component.literal(String.valueOf(PCBox)).withStyle(ChatFormatting.BOLD))
            );

            this.addRenderableWidget(jumpPCBoxWidget);
        }
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/cobblemon/mod/common/client/render/RenderHelperKt;drawScaledText$default(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/chat/MutableComponent;Ljava/lang/Number;Ljava/lang/Number;FLjava/lang/Number;IIZZLjava/lang/Integer;Ljava/lang/Integer;ILjava/lang/Object;)V",
                    ordinal = 12// Target the 13th Box Label occurrence (0-based index)
            ),
            index = 2
    )
    private MutableComponent modifyPCBoxLabelLogic(MutableComponent originalText) {
        if(config.pcBoxJump) {
            if (jumpPCBoxWidget.isFocused()) {
                return Component.empty();
            }
        }
        return originalText;
    }

    //Stop render original PC Box Title and replace with JumpPCBox/rest of code init
    @Inject(
            method = "render",
            at = @At(value = "TAIL")
    )
    private void overridePCRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        var pokemon = previewPokemon;
        var x = (width - BASE_WIDTH) / 2;
        var y = (height - BASE_HEIGHT) / 2;

        if (pokemon != null && config.HAHightlighter){
            HAHighlighterRenderer.INSTANCE.renderPC(context,x,y,pokemon);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @ModifyVariable(
            method = "<init>(Lcom/cobblemon/mod/common/client/storage/ClientPC;Lcom/cobblemon/mod/common/client/storage/ClientParty;Lcom/cobblemon/mod/common/client/gui/pc/PCGUIConfiguration;I)V",
            at = @At("HEAD"), // Modify the variable as soon as the constructor starts
            index = 4,        // The 5th parameter (0-based: this, 1st, 2nd, 3rd, 4th)
            name = "openOnBox",
            argsOnly = true,
            remap = false
    )
    private static int fixOpenOnBox(int value) {
        if(FabricLoader.getInstance().isModLoaded("cobblemon-ui-tweaks") && config.uiTweaks_lastPCBoxFix) {
            try{
                Class<?> guiHandlerClass = Class.forName("ca.landonjw.GUIHandler");
                Object instance = guiHandlerClass.getField("INSTANCE").get(null);
                return (int) guiHandlerClass.getMethod("getLastPCBox").invoke(instance);
            }
            catch (Exception e) { LOGGER.info(e.toString()); }
        }
        return value;
    }
}
