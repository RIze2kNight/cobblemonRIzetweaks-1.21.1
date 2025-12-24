package com.rize2knight

import com.rize2knight.config.ModConfig
import com.rize2knight.keybind.RIzeTweakKeyRegistry
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer
import net.fabricmc.api.ClientModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory


object CobblemonRizeTweaksClient : ClientModInitializer {
	val LOGGER: Logger = LoggerFactory.getLogger("cobblemonrizetweaks")
	const val MODID = "cobblemonrizetweaks"
	var config: ModConfig? = null

	override fun onInitializeClient() {
		LOGGER.info("CobblemonRIzeTweaksClient running/initializing")

        //Config Registry
		AutoConfig.register(ModConfig::class.java) { definition: Config?, configClass: Class<ModConfig?>? ->
			JanksonConfigSerializer(
				definition,
				configClass
			)
		}
		config = AutoConfig.getConfigHolder(ModConfig::class.java).config

        //Keybind Registry
        RIzeTweakKeyRegistry.register()
	}
}