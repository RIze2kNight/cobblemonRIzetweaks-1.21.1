package com.rize2knight.util

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.types.ElementalType

object MoveEffectivenessCalculator {
    private val typeChart: HashMap<String, HashMap<String, Float>> = HashMap()
    private val graalTypeChart: GraalTypeChart = GraalTypeChart()

    init {
        graalTypeChart.openConnection()
        graalTypeChart.getTypeChart(typeChart)
    }

    // Calculates the damage multiplier of a move against a defender with up to two types
    fun getMoveDamageMult(move: MoveTemplate, defenderType1: ElementalType?, defenderType2: ElementalType?): Float {
        val moveType = move.elementalType
        var damageMult = 1f

        listOfNotNull(defenderType1, defenderType2).forEach { type ->
            move.let { damageMult *= getDamageMult(it.name, type.name) }
            damageMult *= getDamageMult(moveType.showdownId, type.showdownId)
        }

        return damageMult
    }

    private fun getDamageMult(move: String, defendType: String): Float {
        if(typeChart[move] == null || typeChart[defendType] == null) return 1f
        return typeChart[defendType]!![move]!!
    }
}