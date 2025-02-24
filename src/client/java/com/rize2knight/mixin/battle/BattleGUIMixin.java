package com.rize2knight.mixin.battle;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.client.gui.battle.BattleGUI;
import com.rize2knight.EffectivenessRenderer;
import com.rize2knight.config.ModConfig;
import com.rize2knight.util.BattleMessageQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static com.cobblemon.mod.common.api.events.CobblemonEvents.*;

@Mixin(BattleGUI.class)
public class BattleGUIMixin {
    @Unique
    private boolean isSubscribed = false;
    @Unique final Map<String, List<ElementalType>> battleTypeChanges = new HashMap<>();

    @Inject(method = "render", at = @At("TAIL"))
    private void render(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if(ModConfig.getInstance().isEnabled("move_tips")) {
            ensureSubscribed();
            EffectivenessRenderer.INSTANCE.render(context, mouseX, mouseY, battleTypeChanges);
        }
    }

    @Unique
    private void ensureSubscribed() {
        if (!isSubscribed) {
            isSubscribed = true;

            // Clears BattleMessageQueue when battle ends
            BattleMessageQueue battleMessageQueue = BattleMessageQueue.INSTANCE;
            BATTLE_STARTED_PRE.subscribe(Priority.NORMAL, battleStartedPreEvent -> {battleMessageQueue.clearBattleMessages(); return null;});

            BattleMessageQueue.subscribe(message -> {
                if(message.getContents() instanceof TranslatableContents contents){
                    Object[] args = contents.getArgs();
                    if(args.length >= 2) {
                        String[] newType = args[1].toString().split("/");
                        if (ElementalTypes.INSTANCE.get(newType[0]) != null) {
                            String owner = null;
                            String pokemonName = null;
                            List<ElementalType> newTypes = new ArrayList<>();

                            for (String type : newType) { newTypes.add(ElementalTypes.INSTANCE.get(type)); }

                            // Extract owner and Pokémon name from the first argument
                            if (args[0] instanceof MutableComponent PokemonComponent
                                    && PokemonComponent.getContents() instanceof TranslatableContents PokemonContents) {
                                if ("cobblemon.battle.owned_pokemon".equals(PokemonContents.getKey())) {
                                    Object[] pokeArgs = PokemonContents.getArgs();
                                    owner = pokeArgs[0].toString(); // Owner name
                                    pokemonName = pokeArgs[1].toString();
                                } else {
                                    owner = "WILD";
                                    pokemonName = PokemonContents.toString();
                                }
                            }

                            if (owner != null && pokemonName != null && !Objects.equals(owner, Minecraft.getInstance().getUser().getName())) {
                                // Create a unique key based on owner and Pokémon name
                                String key = owner + ":" + pokemonName;
                                battleTypeChanges.put(key, newTypes); // Update or add the new type
                            }
                        }
                    }
                }
                return null;
            });
        }
    }
}