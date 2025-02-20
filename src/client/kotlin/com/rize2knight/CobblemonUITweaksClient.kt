package com.rize2knight

import net.fabricmc.api.ClientModInitializer
import org.slf4j.LoggerFactory

object CobblemonUITweaksClient : ClientModInitializer {
	val logger = LoggerFactory.getLogger("cobblemonrizetweaks")
	const val MODID = "cobblemonrizetweaks"

	override fun onInitializeClient() {
		logger.info("CobblemonRIzeTweaksClient running/initializing")
	}
}