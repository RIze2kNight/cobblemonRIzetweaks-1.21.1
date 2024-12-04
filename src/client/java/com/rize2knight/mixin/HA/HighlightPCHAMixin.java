package com.rize2knight.mixin.HA;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.rize2knight.HAHighlighterRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.cobblemon.mod.common.client.gui.pc.PCGUI.BASE_HEIGHT;
import static com.cobblemon.mod.common.client.gui.pc.PCGUI.BASE_WIDTH;

@Mixin(PCGUI.class)
public class HighlightPCHAMixin extends Screen {

    @Shadow(remap = false)
    private Pokemon previewPokemon = null;

    protected HighlightPCHAMixin(Component component) {
        super(component);
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/cobblemon/mod/common/client/render/RenderHelperKt;drawScaledText$default(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/chat/MutableComponent;Ljava/lang/Number;Ljava/lang/Number;FLjava/lang/Number;IIZZLjava/lang/Integer;Ljava/lang/Integer;ILjava/lang/Object;)V",
                    ordinal = 10 ,   // Target the 11th Box Label occurrence (0-based index)
                    shift = At.Shift.AFTER // Inject after the invocation
            )
    )
    private void higlightPCHA(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        var x = (width - BASE_WIDTH) / 2;
        var y = (height - BASE_HEIGHT) / 2;
        var pokemon = previewPokemon;

        if(pokemon != null){
            HAHighlighterRenderer.INSTANCE.renderPC(context,x,y,pokemon);
        }
    }
}
