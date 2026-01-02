package com.rize2knight.mixin.pc;

import com.cobblemon.mod.common.client.gui.pc.BoxNameWidget;
import com.cobblemon.mod.common.client.gui.summary.Summary;
import com.cobblemon.mod.common.client.keybind.CobblemonKeyBinds;
import com.cobblemon.mod.common.mixin.accessor.KeyBindingAccessor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.client.gui.pc.StorageWidget;
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbility;
import com.rize2knight.CobblemonRizeTweaksClient;
import com.rize2knight.config.ModConfig;
import com.rize2knight.util.RIzeTweaksUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = PCGUI.class, priority = 1001)
public abstract class PCGUIMixin extends Screen {
    @Unique private static ModConfig config = CobblemonRizeTweaksClient.INSTANCE.getConfig();

    @Shadow(remap = false) private StorageWidget storageWidget;
    @Shadow public abstract void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta);
    @Shadow(remap = false) private Pokemon previewPokemon = null;
    @Shadow(remap = false) private BoxNameWidget boxNameWidget;
    @Mutable @Shadow(remap = false) @Final private int openOnBox;

    @Unique private Integer lastBox = -1;

    protected PCGUIMixin(Component component) {
        super(component);
        CobblemonRizeTweaksClient.LOGGER.info("Initializing PCGUIMixin with storageWidget: {}", storageWidget);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        //Checks if PC Boxes have been scrolled through
        int current = storageWidget.getBox();
        if (current == lastBox) return;
        lastBox = current;

        if (boxNameWidget == null) return;

        //Unfocuses jumpBox / Modified Box name if enabled and toggles off
        if(boxNameWidget.isFocused() && RIzeTweaksUtil.enablePCBoxJump){
            boxNameWidget.setFocused(false);
            RIzeTweaksUtil.enablePCBoxJump = false;
        }
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/cobblemon/mod/common/client/render/RenderHelperKt;drawScaledText$default(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/chat/MutableComponent;Ljava/lang/Number;Ljava/lang/Number;FLjava/lang/Number;IIZZLjava/lang/Integer;Ljava/lang/Integer;ILjava/lang/Object;)V",
                    ordinal = 12
            ),
            index = 2
    )
    private MutableComponent overrideAbilityNameDisplay(MutableComponent text){
        //Replace Ability Text with HA highlighted text
        if(previewPokemon != null && config.HAHightlighter){
            if(hasHiddenAbility(previewPokemon)){
                return text.withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFD700)));
            }
        }
        return text;
    }

    @Inject(method = "keyPressed", at = @At(value = "HEAD"))
    private void overrideKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir){
        var summaryKey = (KeyBindingAccessor) CobblemonKeyBinds.INSTANCE.getSUMMARY();

        //Opens Summary from PC when Summary Key is pressed
        if(keyCode == summaryKey.boundKey().getValue()){
            try{
                //Updates PC to open on the most recent PC Box set
                this.openOnBox = this.storageWidget.getBox();

                RIzeTweaksUtil.summaryOpenedFromPC = true;
                RIzeTweaksUtil.pcGUI = (PCGUI) Minecraft.getInstance().screen;

                Summary.Companion.open(List.of(previewPokemon),true,0);
            }
            catch (Exception e) {
                CobblemonRizeTweaksClient.LOGGER.debug("Failed to open summary inside PC");
            }
        }
    }

    @Unique
    private boolean hasHiddenAbility(Pokemon pokemon) {
        for (var ability : pokemon.getForm().getAbilities()) {
            if (ability instanceof HiddenAbility hidden) {
                if (hidden.getTemplate().equals(pokemon.getAbility().getTemplate())) {
                    return true;
                }
            }
        }
        return false;
    }
}
