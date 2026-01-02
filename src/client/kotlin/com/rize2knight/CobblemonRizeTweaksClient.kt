package com.rize2knight

import com.rize2knight.config.ModConfig
import com.rize2knight.keybind.RIzeTweakKeyRegistry
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer
import net.fabricmc.api.ClientModInitializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


object CobblemonRizeTweaksClient : ClientModInitializer {
	@JvmField
    val LOGGER: Logger = LogManager.getLogger("cobblemonrizetweaks")
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