package com.rize2knight.util

import com.cobblemon.mod.common.api.text.*
import com.cobblemon.mod.common.util.asTranslated
import net.minecraft.network.chat.MutableComponent

object UITweaksEffectiveness {
    private var effectivenessText: MutableComponent? = null

    @JvmStatic
    fun set(effectiveness: Float) {
        effectivenessText = when {
            effectiveness == 0f -> "cobblemon_ui_tweaks.move.effectiveness.immune".asTranslated().bold().italicise().darkRed()
            effectiveness < 1f  -> "cobblemon_ui_tweaks.move.effectiveness.not_very_effective".asTranslated("${effectiveness}x").bold().italicise().yellow()
            effectiveness > 1f -> "cobblemon_ui_tweaks.move.effectiveness.super_effective".asTranslated("${effectiveness}x").bold().italicise().green()
            else -> "cobblemon_ui_tweaks.move.effectiveness.neutral".asTranslated().bold().italicise()
        }
    }

    @JvmStatic
    fun get(): MutableComponent? {
        return effectivenessText
    }
}