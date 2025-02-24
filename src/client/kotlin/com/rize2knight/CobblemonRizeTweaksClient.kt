package com.rize2knight

import com.rize2knight.config.ModConfig
import net.fabricmc.api.ClientModInitializer
import org.slf4j.LoggerFactory

object CobblemonRizeTweaksClient : ClientModInitializer {
	val LOGGER = LoggerFactory.getLogger("cobblemonrizetweaks")
	const val MODID = "cobblemonrizetweaks"

	override fun onInitializeClient() {
		ModConfig.loadConfig()
		LOGGER.info("CobblemonRIzeTweaksClient running/initializing")
	}
}