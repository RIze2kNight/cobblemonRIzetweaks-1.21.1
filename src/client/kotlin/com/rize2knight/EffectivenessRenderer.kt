package com.rize2knight

import ca.landonjw.util.ReflectionUtils
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.moves.categories.DamageCategories
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.types.ElementalType
import com.cobblemon.mod.common.api.types.ElementalTypes
import com.cobblemon.mod.common.battles.ai.TypeEffectivenessMap
import com.cobblemon.mod.common.battles.ai.getDamageMultiplier
import com.cobblemon.mod.common.battles.ai.typeEffectiveness
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonClient.battle
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.battle.ActiveClientBattlePokemon
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.COMPACT_INFO_OFFSET_X
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.COMPACT_PORTRAIT_DIAMETER
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.COMPACT_PORTRAIT_OFFSET_X
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.COMPACT_PORTRAIT_OFFSET_Y
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.COMPACT_TILE_HEIGHT
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.COMPACT_TILE_TEXTURE_HEIGHT
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.COMPACT_TILE_WIDTH
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.COMPACT_VERTICAL_SPACING
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.INFO_OFFSET_X
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.PORTRAIT_DIAMETER
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.PORTRAIT_OFFSET_X
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.PORTRAIT_OFFSET_Y
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.SCALE
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.TILE_HEIGHT
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.TILE_WIDTH
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.VERTICAL_INSET
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.VERTICAL_SPACING
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.battleInfoBase
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.battleInfoBaseCompact
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.battleInfoBaseFlipped
import com.cobblemon.mod.common.client.gui.battle.BattleOverlay.Companion.battleInfoBaseFlippedCompact
import com.cobblemon.mod.common.client.gui.battle.subscreen.BattleMoveSelection
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.relocations.oracle.truffle.api.dsl.TypeCheck
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.resources.language.I18n
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.PlainTextContents
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.ResourceLocation

object EffectivenessRenderer {
//    val leftGreen = ResourceLocation.tryBuild(CobblemonUITweaks.MODID, "textures/battle/move/header/left.png")
//    val rightGreen = ResourceLocation.tryBuild(CobblemonUITweaks.MODID, "textures/battle/move/header/middle.png")
    val leftNeutral = ResourceLocation.tryBuild(CobblemonUITweaksClient.MODID, "textures/battle/effectiveness/battle_effective.png")
    val rightNeutral = ResourceLocation.tryBuild(CobblemonUITweaksClient.MODID, "textures/battle/effectiveness/battle_effective_flipped.png")
//    val leftYellow = ResourceLocation.tryBuild(CobblemonUITweaks.MODID, "textures/battle/move/header/left.png")
//    val rightYellow = ResourceLocation.tryBuild(CobblemonUITweaks.MODID, "textures/battle/move/header/middle.png")
    val leftNeutralComp = ResourceLocation.tryBuild(CobblemonUITweaksClient.MODID, "textures/battle/effectiveness/battle_effective_compact.png")
    val rightNeutralComp = ResourceLocation.tryBuild(CobblemonUITweaksClient.MODID, "textures/battle/effectiveness/battle_effective_flipped_compact.png")
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

        val selectableTargetList = move.target.targetList(playerPokemon!!)
        val multiTargetList = if(selectableTargetList == null) playerPokemon!!.getMultiTargetList(move.target) else null
        val isTarget = selectableTargetList?.firstOrNull { it.getPNX() == activeBattlePokemon.getPNX() }?.getPNX()
        val isMultiTarget = multiTargetList?.any { it.getPNX() == activeBattlePokemon.getPNX() }

        if(isTarget != null || isMultiTarget == true){
            val playerNumberOffset = (activeBattlePokemon.getActorShowdownId()[1].digitToInt() - 1) / 2 * 10
            val isCompact = battle?.battleFormat?.battleType?.pokemonPerSide!! > 1

            val tileWidth = if(isCompact) COMPACT_TILE_WIDTH else TILE_WIDTH
            val tileHeight = if(isCompact) COMPACT_TILE_HEIGHT else TILE_HEIGHT
            val verticalSpacing = if(isCompact) COMPACT_VERTICAL_SPACING else VERTICAL_SPACING
            val portraitDiameter = if (isCompact) COMPACT_PORTRAIT_DIAMETER else PORTRAIT_DIAMETER
            val portraitOffsetX = if (isCompact) COMPACT_PORTRAIT_OFFSET_X else PORTRAIT_OFFSET_X
            val portraitOffsetY = if (isCompact) COMPACT_PORTRAIT_OFFSET_Y else PORTRAIT_OFFSET_Y
            val infoOffsetX = if (isCompact) COMPACT_INFO_OFFSET_X else INFO_OFFSET_X
            val offset = if(isCompact) 30 else 40

            val x = activeBattlePokemon.xDisplacement
            val y = VERTICAL_INSET + rank * verticalSpacing + (if (left) playerNumberOffset else (battle!!.battleFormat.battleType.actorsPerSide - 1) * 10 - playerNumberOffset)

            val x0 = x + if (left) tileWidth  else 0
            val y0 = y + portraitOffsetY

            val effectivenessText = getMoveEffectiveness(moveTile!!.moveTemplate, activeBattlePokemon)
            val matrixStack = context.pose()

            blitk(
                matrixStack = matrixStack,
                texture = if (isCompact) (if (left) leftNeutralComp else rightNeutralComp) else (if (left) leftNeutral else rightNeutral),
                x = x - if(left) 0 else ((PORTRAIT_DIAMETER / 2) + PORTRAIT_OFFSET_X + 1),
                y = y,
                height = tileHeight,
                width = tileWidth + (PORTRAIT_DIAMETER / 2) + PORTRAIT_OFFSET_X + 1,
                textureHeight = if (isCompact) COMPACT_TILE_TEXTURE_HEIGHT else TILE_HEIGHT,
            )
            if (effectivenessText != null) {
                drawScaledText(
                    context = context,
                    font = CobblemonResources.DEFAULT_LARGE,
                    text = effectivenessText.bold(),
                    x = if(left) x0 + (if(isCompact) 0 else 3) else x - 14,
                    y = y0,
                    shadow = true
                )
            }
        }
    }

    private fun getMoveEffectiveness(move: MoveTemplate, activeBattlePokemon: ActiveClientBattlePokemon): MutableComponent? {
        val opponent = activeBattlePokemon.battlePokemon ?: return null
        val aspects: Set<String> = ReflectionUtils.getPrivateField(opponent, "aspects") ?: return null

        val opponentForm = opponent.species.getForm(aspects)
        if (move.damageCategory == DamageCategories.STATUS) return null

        val typeChangeList = getTypeChanges(activeBattlePokemon)
        var primaryType: ElementalType? = opponentForm.primaryType
        var secondaryType: ElementalType? = opponentForm.secondaryType

        if (typeChangeList != null) {
            if (typeChangeList.isNotEmpty()) {
                primaryType = typeChangeList.getOrNull(0) ?: primaryType // Use the first element if available, otherwise keep the original type
                secondaryType = typeChangeList.getOrNull(1) // Use the second element if available, otherwise keep the original type
            }
        }

        val primaryDmg : Double = primaryType?.let { getDamageMultiplier(move.elementalType, it) }!!
        val secondaryDmg : Double = secondaryType?.let { getDamageMultiplier(move.elementalType, it) } ?: 1.0

        return Component.literal("x" + (primaryDmg * secondaryDmg))
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