package com.rize2knight.mixin.HA;

import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget;
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.info.InfoWidget;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.rize2knight.HAHighlighterRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InfoWidget.class)
public abstract class HighlightSummaryHAMixin extends SoundlessWidget {
    @Shadow(remap = false) @Final private static int ROW_HEIGHT;
    @Shadow(remap = false) @Final private Pokemon pokemon;

    public HighlightSummaryHAMixin(int pX, int pY, int pWidth, int pHeight, @NotNull Component component) {
        super(pX, pY, pWidth, pHeight, component);
    }

    @Inject(
        method = "renderWidget",
        at = @At(
                value = "RETURN"
        )
    )
    protected void renderWidget(GuiGraphics context, int pMouseX, int pMouseY, float pPartialTicks, CallbackInfo ci) {
        if(pokemon != null){
            var abilityWidget = HAHighlighterRenderer.INSTANCE.renderSummary(context,this.getX(),this.getY(),this.ROW_HEIGHT,this.width,pokemon);
            if(abilityWidget != null){
                abilityWidget.render(context, pMouseX, pMouseY, pPartialTicks);
            }
        }
    }


}
