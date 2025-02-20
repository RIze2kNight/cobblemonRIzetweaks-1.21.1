package com.rize2knight.mixin.battle.move;

import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection;
import com.rize2knight.EffectivenessRenderer;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BattleMoveSelection.class)
public class BattleMoveSelectionMixin {
    @Shadow(remap = false) private List<BattleMoveSelection.MoveTile> moveTiles;

    @Inject(method = "renderWidget", at = @At("TAIL"))
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.moveTiles.forEach(tile -> {
            if(tile.isHovered(mouseX, mouseY)) {
                EffectivenessRenderer.INSTANCE.setMoveTile(tile);
            }
        });
    }
}
