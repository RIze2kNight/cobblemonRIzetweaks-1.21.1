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