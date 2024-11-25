package com.rize2knight

import net.fabricmc.api.ClientModInitializer
import org.slf4j.LoggerFactory

object CobblemonUITweaksClient : ClientModInitializer {
	private val logger = LoggerFactory.getLogger("cobblemonuitweaks")
	const val MODID = "cobblemonuitweaks"

	override fun onInitializeClient() {
		logger.info("CobblemonUITweaksClient running/initializing")
	}
}