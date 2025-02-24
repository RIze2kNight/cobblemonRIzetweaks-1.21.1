package com.rize2knight

import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.api.types.ElementalType
import com.rize2knight.util.GraalTypeChart

object MoveEffectivenessCalculator {
    private val typeChart: HashMap<String, HashMap<String, Float>> = HashMap()
    private val graalTypeChart: GraalTypeChart = GraalTypeChart()

    init {
        graalTypeChart.openConnection()
        graalTypeChart.getTypeChart(typeChart)
    }

    fun getMoveDamageMult(move: MoveTemplate, defenderType1: ElementalType?, defenderType2: ElementalType?): Float {
        val moveType = move.elementalType
        var damageMult = 1f

        listOfNotNull(defenderType1, defenderType2).forEach { type ->
            move.let { damageMult *= getDamageMult(it.name, type.name) }
            damageMult *= getDamageMult(moveType.name, type.name)
        }

        return damageMult
    }

    private fun getDamageMult(move: String, defendType: String): Float {
        if(typeChart[move] == null || typeChart[defendType] == null) return 1f
        return typeChart[defendType]!![move]!!
    }
}