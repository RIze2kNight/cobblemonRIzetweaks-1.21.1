package com.rize2knight.mixin.battle.move;

import ca.landonjw.ResizeableTextQueue;
import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection;
import com.rize2knight.BattleTypeList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

import static com.cobblemon.mod.common.api.events.CobblemonEvents.*;

@Mixin(BattleMoveSelection.class)
public class BattleMoveSelectionMixin {
    @Unique private static final Logger LOGGER = LoggerFactory.getLogger("cobblemonrizetweaks");
    @Shadow(remap = false) private List<BattleMoveSelection.MoveTile> moveTiles;

    @Unique private boolean isSubscribed = false;

    @Inject(method = "renderWidget", at = @At("TAIL"))
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ensureSubscribed();
    }

    private void ensureSubscribed() {
        if (!isSubscribed) {
            isSubscribed = true;

            BATTLE_VICTORY.subscribe(Priority.NORMAL, battleEndEvent -> {BattleTypeList.clearBattleTypeChanges();return null;});
            BATTLE_FLED.subscribe(Priority.NORMAL, battleEndEvent -> {BattleTypeList.clearBattleTypeChanges();return null;});
            BATTLE_FAINTED.subscribe(Priority.NORMAL, battleEndEvent -> {BattleTypeList.clearBattleTypeChanges();return null;});
            POKEMON_CAPTURED.subscribe(Priority.NORMAL, battleEndEvent -> {BattleTypeList.clearBattleTypeChanges();return null;});

            ResizeableTextQueue queueWithBattleMessages = (ResizeableTextQueue) (Object) CobblemonClient.INSTANCE.getBattle().getMessages();
            queueWithBattleMessages.cobblemon_ui_tweaks$subscribe(message -> {
                if(message.getContents() instanceof TranslatableContents contents
                        && "cobblemon.battle.start.typechange".equals(contents.getKey())){
                    Object[] args = contents.getArgs();

                    if(args.length >= 2){
                        String owner = null;
                        String pokemonName = null;
                        String newType = args[1].toString(); // Second argument is the type

                        // Extract owner and Pokémon name from the first argument
                        if (args[0] instanceof MutableComponent PokemonComponent
                                && PokemonComponent.getContents() instanceof TranslatableContents PokemonContents) {

                            if("cobblemon.battle.owned_pokemon".equals(PokemonContents.getKey())){
                                Object[] pokeArgs = PokemonContents.getArgs();
                                owner = pokeArgs[0].toString(); // Owner name
                                pokemonName = pokeArgs[1].toString();
                            }
                            else {
                                owner = "WILD";
                                pokemonName = PokemonContents.toString();
                            }
                        }
                        if (owner != null && pokemonName != null && !Objects.equals(owner, Minecraft.getInstance().getUser().getName())) {
                            // Create a unique key based on owner and Pokémon name
                            String key = owner + ":" + pokemonName;
                            BattleTypeList.addTypeChange(key, newType); // Update or add the new type
                        }
                    }
                }
            });
        }
    }
}
