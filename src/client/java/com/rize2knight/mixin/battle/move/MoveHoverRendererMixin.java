package com.rize2knight.mixin.battle.move;


import ca.landonjw.MoveEffectivenessCalculator;
import ca.landonjw.MoveHoverRenderer;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.moves.categories.DamageCategories;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.battle.ClientBattle;
import com.rize2knight.BattleTypeList;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.*;

@Mixin(value = MoveHoverRenderer.class)
public abstract class MoveHoverRendererMixin {
    @Unique private static final Logger LOGGER = LoggerFactory.getLogger("cobblemonrizetweaks");

    @Inject(method = "getMoveEffectiveness", at = @At("HEAD"), cancellable = true)
    public void getMoveEffectiveness(MoveTemplate move, CallbackInfoReturnable<MutableComponent> ci){
        if (!FabricLoader.getInstance().isModLoaded("cobblemon-ui-tweaks")) {
            return; // Skip if the mod is not present
        }
        var battle = CobblemonClient.INSTANCE.getBattle();
        if (battle == null) { return; }

        var opponent = battle.getSide2().getActiveClientBattlePokemon().iterator().next().getBattlePokemon();
        if (opponent == null) { return; }

        var aspects = opponent.getProperties().getAspects();
        var opponentForm = opponent.getSpecies().getForm(aspects);
        if (move.getDamageCategory() == DamageCategories.INSTANCE.getSTATUS()) {
            return;
        }

        List<ElementalType> typeChangeList = getTypeChanges(battle, BattleTypeList.getBattleTypeChanges());
        ElementalType primaryType = opponentForm.getPrimaryType();
        ElementalType secondaryType = opponentForm.getSecondaryType();

        if (!typeChangeList.isEmpty()) {
            primaryType = typeChangeList.get(0);
            secondaryType = typeChangeList.size() > 1 ? typeChangeList.get(1) : secondaryType;
        }

        ci.setReturnValue(MoveEffectivenessCalculator.INSTANCE.getMoveEffectiveness(move.getElementalType(), primaryType, secondaryType));
        ci.cancel();
    }

    private List<ElementalType> getTypeChanges(ClientBattle battle, Map<String, String> typeChanges){
        List<ElementalType> typeChangeList = new ArrayList<>();
        var owner = battle.getSide2().getActors().getFirst().getDisplayName().getContents();
        var opponent = Objects.requireNonNull(battle.getSide2().getActiveClientBattlePokemon().iterator().next().getBattlePokemon()).getDisplayName().getContents();

        var ownerName = owner instanceof TranslatableContents
                ? I18n.get(((TranslatableContents) owner).getKey())
                : I18n.get(((PlainTextContents) owner).text());
        var opponentName = opponent instanceof TranslatableContents
                ? I18n.get(((TranslatableContents) opponent).getKey())
                : I18n.get(((PlainTextContents) opponent).text());

        String[] currentType = new String[0];
        if(battle.getSide2().getActors().getFirst().getType().name().equals("WILD")){
            if(typeChanges.get("WILD:" + opponentName) != null){
                currentType = typeChanges.get("WILD:" + opponentName).split("/");
            }
        }
        else if(typeChanges.get(ownerName + ":" + opponentName) != null){
            currentType = typeChanges.get(ownerName + ":" + opponentName).split("/");
        }

        for(String element : currentType){
            typeChangeList.add(ElementalTypes.INSTANCE.get(element));
        }

        return typeChangeList;
    }
}
