package com.rize2knight

import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.categories.DamageCategories
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.font
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonClient.battle
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.COMPACT_TILE_HEIGHT
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.COMPACT_TILE_WIDTH
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.COMPACT_VERTICAL_SPACING
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.TILE_HEIGHT
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.TILE_WIDTH
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.VERTICAL_INSET
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.VERTICAL_SPACING
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection
import com.cobblemon.mod.common.client.render.drawScaledText
import com.rize2knight.config.ModConfig
import com.rize2knight.util.ReflectionUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.PlainTextContents
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.ResourceLocation

object EffectivenessRenderer {
    private val singleTarget = ResourceLocation.tryBuild(CobblemonRizeTweaksClient.MODID, "textures/battle/effectiveness/range_single.png")
    private val multiTarget = ResourceLocation.tryBuild(CobblemonRizeTweaksClient.MODID, "textures/battle/effectiveness/range_multi.png")
    private val leftNeutral = ResourceLocation.tryBuild(CobblemonRizeTweaksClient.MODID, "textures/battle/effectiveness/battle_effective.png")
    private val rightNeutral = ResourceLocation.tryBuild(CobblemonRizeTweaksClient.MODID, "textures/battle/effectiveness/battle_effective_flipped.png")
    private val leftGreen = ResourceLocation.tryBuild(CobblemonRizeTweaksClient.MODID, "textures/battle/effectiveness/battle_green.png")
    private val rightGreen = ResourceLocation.tryBuild(CobblemonRizeTweaksClient.MODID, "textures/battle/effectiveness/battle_green_flipped.png")
    private val leftYellow = ResourceLocation.tryBuild(CobblemonRizeTweaksClient.MODID, "textures/battle/effectiveness/battle_yellow.png")
    private val rightYellow = ResourceLocation.tryBuild(CobblemonRizeTweaksClient.MODID, "textures/battle/effectiveness/battle_yellow_flipped.png")
    private val leftRed = ResourceLocation.tryBuild(CobblemonRizeTweaksClient.MODID, "textures/battle/effectiveness/battle_red.png")
    private val rightRed = ResourceLocation.tryBuild(CobblemonRizeTweaksClient.MODID, "textures/battle/effectiveness/battle_red_flipped.png")


    private val playerUUID = Minecraft.getInstance().player?.uuid
    private var playerPokemon : ActiveClientBattlePokemon? = null
    private var typeChangesList : Map<String,MutableList<ElementalType>>? = null
    var moveTile : BattleMoveSelection.MoveTile? = null

    fun render(context: GuiGraphics, x: Float, y: Float, typeChanges: Map<String,MutableList<ElementalType>>){
        val battle = CobblemonClient.battle ?: return
        val playerUUID = playerUUID ?: return
        if(moveTile == null) return

        typeChangesList = typeChanges

        val playerSide = if (battle.side1.actors.any { it.uuid == playerUUID }) battle.side1 else battle.side2
        val opponentSide = if (playerSide == battle.side1) battle.side2 else battle.side1

        playerPokemon = playerSide.actors.find { it.uuid == playerUUID }?.activePokemon?.find { it.battlePokemon?.uuid == moveTile!!.pokemon?.uuid }
        playerSide.activeClientBattlePokemon.forEachIndexed { index, activeClientBattlePokemon ->
            renderIfHovered(context, activeClientBattlePokemon, true, index)
        }

        opponentSide.activeClientBattlePokemon.reversed().forEachIndexed { index, activeClientBattlePokemon ->
            renderIfHovered(context, activeClientBattlePokemon, false, index)
        }

        // Prevents Renderer to always be visible
        moveTile = null
    }

    private fun renderIfHovered(context: GuiGraphics, activeBattlePokemon: ActiveClientBattlePokemon, left: Boolean, rank: Int) {
        // Prevent render if pokemon is currently being swapped out or is player pokemon
        if (activeBattlePokemon.animations.peek() !== null ||
            playerPokemon == null) return
        val move = moveTile?.move ?: return
        val isPlayer = activeBattlePokemon.getPNX() == playerPokemon!!.getPNX()

        val selectableTargetList = move.target.targetList(playerPokemon!!)
        val multiTargetList = if(selectableTargetList == null) playerPokemon!!.getMultiTargetList(move.target) else null
        val isTarget = selectableTargetList?.firstOrNull { it.getPNX() == activeBattlePokemon.getPNX() }?.getPNX()
//        val isMultiTarget = multiTargetList?.any { it.getPNX() == activeBattlePokemon.getPNX() }


        if((isTarget != null || multiTargetList != null || isPlayer) && moveTile!!.moveTemplate.damageCategory != DamageCategories.STATUS) {
            val playerNumberOffset = (activeBattlePokemon.getActorShowdownId()[1].digitToInt() - 1) / 2 * 10
            val isCompact = battle?.battleFormat?.battleType?.pokemonPerSide!! > 1

            val tileWidth = if(isCompact) COMPACT_TILE_WIDTH else TILE_WIDTH
            val tileHeight = if(isCompact) COMPACT_TILE_HEIGHT else TILE_HEIGHT
            val verticalSpacing = if(isCompact) COMPACT_VERTICAL_SPACING else VERTICAL_SPACING
            val textureWidth = TILE_WIDTH + 26

            val x = activeBattlePokemon.xDisplacement
            val y = VERTICAL_INSET + rank * verticalSpacing + (if (left) playerNumberOffset else (battle!!.battleFormat.battleType.actorsPerSide - 1) * 10 - playerNumberOffset)

            val x0 = x + if (left) tileWidth  else -1
            val dmgMultiplier = getMoveEffectiveness(moveTile!!.moveTemplate, activeBattlePokemon)
            val effectivenessText = Component.literal("x$dmgMultiplier")
            val matrixStack = context.pose()

            val leftTexture = when {
                dmgMultiplier!! > 1f -> leftGreen
                dmgMultiplier > 0f && dmgMultiplier < 1f -> leftYellow
                dmgMultiplier == 0f -> leftRed
                else -> leftNeutral
            }
            val rightTexture = when {
                dmgMultiplier > 1f -> rightGreen
                dmgMultiplier > 0f && dmgMultiplier < 1f -> rightYellow
                dmgMultiplier == 0f -> rightRed
                else -> rightNeutral
            }

            blitk(
                matrixStack = matrixStack,
                texture = if(isPlayer) (if(multiTargetList != null) multiTarget else singleTarget)
                            else (if (left) leftTexture else rightTexture),
                x = x - if(left) 0 else if(isCompact) 28 else 26,
                y = y,
                height = tileHeight,
                width = textureWidth,
                textureHeight = TILE_HEIGHT + COMPACT_TILE_HEIGHT,
                vOffset = if (isCompact) TILE_HEIGHT else 0,
                uOffset = if (!left) if(isCompact) 10 else 0 else 0
            )
            if (effectivenessText != null && !isPlayer) {
                val textRenderer = Minecraft.getInstance().font
                val textOffset = textRenderer.width(effectivenessText.font(CobblemonResources.DEFAULT_LARGE).bold()) / 2
                val centeredOffsetX = if(isCompact) 12 else 21/2

                drawScaledText(
                    context = context,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = effectivenessText.bold(),
                    x = x0 + if(left) centeredOffsetX - textOffset else -(centeredOffsetX + textOffset),
                    y = y + if (isCompact) 6 else 8,
                    shadow = true
                )
            }
        }
    }

    private fun getMoveEffectiveness(move: MoveTemplate, activeBattlePokemon: ActiveClientBattlePokemon): Float? {
        val opponent = activeBattlePokemon.battlePokemon ?: return null
        val aspects: Set<String> = ReflectionUtils.getPrivateField(opponent, "aspects") ?: return null

        val opponentForm = opponent.species.getForm(aspects)

        val typeChangeList = getTypeChanges(activeBattlePokemon)
        var primaryType: ElementalType? = opponentForm.primaryType
        var secondaryType: ElementalType? = opponentForm.secondaryType

        if (typeChangeList != null && ModConfig.getInstance().isEnabled("type_changes")) {
            if (typeChangeList.isNotEmpty()) {
                primaryType = typeChangeList.getOrNull(0) ?: primaryType // Use the first element if available, otherwise keep the original type
                secondaryType = typeChangeList.getOrNull(1) ?: secondaryType // Use the second element if available, otherwise keep the original type
            }
        }

        val damageMultiplier : Float = MoveEffectivenessCalculator.getMoveDamageMult(move, primaryType, secondaryType)

        return damageMultiplier
    }

    private fun getTypeChanges(opponent: ActiveClientBattlePokemon): List<ElementalType>? {
        var typeChangeList: List<ElementalType>? = null
        val ownerName = when (val owner = opponent.actor.displayName.contents){
            is TranslatableContents -> I18n.get(owner.key)
            is PlainTextContents -> I18n.get(owner.text())
            else -> owner.toString()
        }
        val opponentName = opponent.battlePokemon?.displayName?.contents

        if(opponent.actor.type.name == "WILD"){
            if(typeChangesList?.get("WILD:$opponentName") != null){
                typeChangeList = typeChangesList!!["WILD:$opponentName"]
            }
        }
        else if(typeChangesList?.get("$ownerName:$opponentName") != null){
            typeChangeList = typeChangesList!!["$ownerName:$opponentName"]
        }

        return typeChangeList
    }
}