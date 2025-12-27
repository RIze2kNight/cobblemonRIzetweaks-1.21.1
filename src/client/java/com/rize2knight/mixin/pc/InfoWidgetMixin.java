package com.rize2knight.mixin.pc;

import com.cobblemon.mod.common.api.gui.ColourLibrary;
import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget;
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.info.InfoWidget;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbility;
import com.rize2knight.CobblemonRizeTweaksClient;
import com.rize2knight.gui.ScrollableMultiLineLabelK;
import com.rize2knight.config.ModConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static com.rize2knight.util.RIzeTweaksUtil.abilityLabelK;

@Mixin(InfoWidget.class)
public abstract class InfoWidgetMixin
        extends SoundlessWidget
{
    @Unique private ModConfig config = CobblemonRizeTweaksClient.INSTANCE.getConfig();
    @Shadow(remap = false) @Final private Pokemon pokemon;

    public InfoWidgetMixin(int pX, int pY, int pWidth, int pHeight, @NotNull Component component) {
        super(pX, pY, pWidth, pHeight, component);
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void onInit(int pX, int pY, Pokemon pokemon, CallbackInfo ci){
        abilityLabelK = null; //Reset label on init
    }

    @Inject(method = "renderWidget", at = @At(value = "TAIL"), remap = false)
    private void renderWidget(GuiGraphics context, int pMouseX, int pMouseY, float pPartialTicks, CallbackInfo ci){
        //Render Ability Description with custom ScrollableMultiLineLabelK
        var matrices = context.pose();
        var smallTextScale = 0.5F;

        matrices.pushPose();
        matrices.scale(smallTextScale, smallTextScale, 1F);

        if(abilityLabelK == null){
            abilityLabelK = new ScrollableMultiLineLabelK(
                Component.translatable(pokemon.getAbility().getDescription()),
                5.5 / smallTextScale,
                117 / smallTextScale,
                37,
                3
            );
        }

        abilityLabelK.render(
            context,
            (getX() + 8) / smallTextScale,
            (getY() + 94.5) / smallTextScale,
            pMouseX/smallTextScale,
            pMouseY/smallTextScale,
            ColourLibrary.WHITE,
            true
        );

        matrices.popPose();
    }

    @ModifyArg(
        method = "renderWidget",
        at = @At(
                value = "INVOKE",
                target = "Lcom/cobblemon/mod/common/client/gui/summary/widgets/screens/info/InfoOneLineWidget;<init>(IIIILnet/minecraft/network/chat/MutableComponent;Lnet/minecraft/network/chat/MutableComponent;ILkotlin/jvm/internal/DefaultConstructorMarker;)V",
                ordinal = 5
        ),
        index = 5
    )
    protected MutableComponent overrideAbilityDisplay(MutableComponent text) {
        //Replace Ability Text with HA highlighted text
        if(pokemon != null && config.HAHightlighter){
            if(hasHiddenAbility(pokemon)){
                return text.withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFD700)));
            }
        }
        return text;
    }

    @ModifyArg(
            method = "renderWidget",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/cobblemon/mod/common/api/gui/MultiLineLabelK$Companion;create(Lnet/minecraft/network/chat/Component;Ljava/lang/Number;Ljava/lang/Number;)Lcom/cobblemon/mod/common/api/gui/MultiLineLabelK;"),
            index = 0
    )
    private Component overrideSummaryAbilityDesc(Component component){
        return Component.empty(); //Prevent default ability description rendering
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
