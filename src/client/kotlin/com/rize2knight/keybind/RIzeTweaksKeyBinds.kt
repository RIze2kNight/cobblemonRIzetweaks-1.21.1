package com.rize2knight.keybind

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.KeyMapping

object RIzeTweaksKeyBinds {
    val binds = mutableListOf<RIzeTweakKeyBinding>()

    fun registerKeybinds(registrar: (KeyMapping) -> Unit) {
        binds.forEach { registrar(it.mapping) }
    }

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register {
            binds.forEach { it.handleTick() }
        }
    }

    fun <T : RIzeTweakKeyBinding> add(bind: T): T {
        bind.mapping = KeyMapping(
            bind.name,
            bind.defaultKey.type,
            bind.defaultKey.value,
            bind.category
        )
        binds.add(bind)
        return bind
    }
}