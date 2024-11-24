package com.rize2knight

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.client.gui.pc.PCGUI
import com.cobblemon.mod.common.client.gui.summary.Summary
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvent
import kotlin.math.max
import kotlin.math.min

object GUIHandler {

    var PC: PCGUI? = null
    var hoveredPokemon: Pokemon? = null
    var hoveredPokemonType: String? = null
    var lastPCBox: Int = 0
    var lastSummaryTabIndex: Int = 0
    var lastStatsTabIndex: Int = 0

    var battleLogWidth: Int = 153
        set(value) {
            val max = Minecraft.getInstance().window.guiScaledWidth - 20
            if (battleLogX + value <= max) {
                field = value
            }
        }

    var battleLogHeight: Int = 46
        set(value) {
            val max = Minecraft.getInstance().window.guiScaledHeight - 20
            if (battleLogY + value <= max) {
                field = value
            }
        }

    var battleLogX: Double = defaultLogX
        get() {
            val max = Minecraft.getInstance().window.guiScaledWidth - battleLogWidth
            return max(min(field, max.toDouble() - 20), 4.0)
        }

    var battleLogY: Double = defaultLogY
        get() {
            val max = Minecraft.getInstance().window.guiScaledHeight - battleLogHeight
            return max(min(field, max.toDouble() - 20), 4.0)
        }

    val defaultLogX: Double
        get() = Minecraft.getInstance().window.guiScaledWidth - 181.0
    val defaultLogY: Double
        get() = Minecraft.getInstance().window.guiScaledHeight - 85.0

    fun onSummaryPressFromPC(pc: PCGUI) {
        if (hoveredPokemon != null) {
            PC = pc
            Summary.open(listOf(hoveredPokemon, null, null, null, null, null), false)
            playSound(CobblemonSounds.GUI_CLICK)
        }
    }

    fun onSummaryClose() {
        if (PC != null) {
            Minecraft.getInstance().setScreen(PC)
            PC = null
        }
    }

    fun onPCClose() {
        PC = null
        lastStatsTabIndex = 0
        lastSummaryTabIndex = 0
        hoveredPokemonType = null
        hoveredPokemon = null
    }

    private fun playSound(soundEvent: SoundEvent) {
        Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(soundEvent, 1.0F))
    }

}