package com.rize2knight.mixin.pc;

import com.cobblemon.mod.common.client.gui.summary.widgets.SoundlessWidget;
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.info.InfoWidget;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbility;
import com.rize2knight.CobblemonRizeTweaksClient;
import com.rize2knight.config.ModConfig;
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
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InfoWidget.class)
public abstract class InfoWidgetMixin extends SoundlessWidget {
    @Unique private ModConfig config = CobblemonRizeTweaksClient.INSTANCE.getConfig();
    @Shadow(remap = false) @Final private Pokemon pokemon;

    public InfoWidgetMixin(int pX, int pY, int pWidth, int pHeight, @NotNull Component component) {
        super(pX, pY, pWidth, pHeight, component);
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
        if(pokemon != null && config.HAHightlighter){
            if(hasHiddenAbility(pokemon)){
                return text.withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFD700)));
            }
        }
        return text;
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
