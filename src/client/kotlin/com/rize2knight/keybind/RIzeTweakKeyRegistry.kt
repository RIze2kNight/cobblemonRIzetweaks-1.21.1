package com.rize2knight.keybind

import com.rize2knight.keybind.keybinds.JumpPCBoxKey
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper

object RIzeTweakKeyRegistry {
    fun register() {
        RIzeTweaksKeyBinds.add(JumpPCBoxKey)

        // REGISTER EVERY KEYBIND WITH MC
        RIzeTweaksKeyBinds.binds.forEach {
            KeyBindingHelper.registerKeyBinding(it.mapping)
        }

        RIzeTweaksKeyBinds.init()
    }
}