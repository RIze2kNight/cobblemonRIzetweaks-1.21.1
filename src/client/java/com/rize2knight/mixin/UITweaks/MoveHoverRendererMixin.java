package com.rize2knight.mixin.UITweaks;

import ca.landonjw.MoveHoverRenderer;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.battles.BattleFormat;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.rize2knight.CobblemonRizeTweaksClient;
import com.rize2knight.config.BattleGUIRendererStyle;
import com.rize2knight.config.ModConfig;
import com.rize2knight.util.UITweaksEffectiveness;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MoveHoverRenderer.class)
public class MoveHoverRendererMixin{
    @Unique private ModConfig config = CobblemonRizeTweaksClient.INSTANCE.getConfig();

    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    private void RIzeRender(GuiGraphics context, float x, float y, MoveTemplate move, CallbackInfo ci){
        if(config.battleGUIStyle == BattleGUIRendererStyle.RIzeTweaks || config.battleGUIStyle == BattleGUIRendererStyle.DISABLE) { ci.cancel(); }
    }
    /*
    *   Modifies CobblemonUITweaks Move Effectiveness to implement type changes from this mod.
    */
    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/cobblemon/mod/common/client/render/RenderHelperKt;drawScaledText$default(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/chat/MutableComponent;Ljava/lang/Number;Ljava/lang/Number;FLjava/lang/Number;IIZZLjava/lang/Integer;Ljava/lang/Integer;ILjava/lang/Object;)V",
                    ordinal = 3// Target the 4th Box Label occurrence (0-based index)
            ),
            index = 2
    )
    private MutableComponent modifyEffectivenessTextLogic(MutableComponent originalText) {
        MutableComponent effectivenessText = UITweaksEffectiveness.get();
        return effectivenessText != null ? effectivenessText : originalText;
    }
}
