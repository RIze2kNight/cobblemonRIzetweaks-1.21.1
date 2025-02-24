package com.rize2knight

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object CobblemonRizeTweaks : ModInitializer {
    private val logger = LoggerFactory.getLogger("cobblemonrizetweaks")

	override fun onInitialize() {

		logger.info("CobblemonRIzeTweaks running/initializing")
	}
}