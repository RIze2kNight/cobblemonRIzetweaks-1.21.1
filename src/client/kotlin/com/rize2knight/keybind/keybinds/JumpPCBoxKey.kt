package com.rize2knight.keybind.keybinds

import com.rize2knight.keybind.RIzeTweakKeyBinding
import org.lwjgl.glfw.GLFW

object JumpPCBoxKey : RIzeTweakKeyBinding (
    "key.cobblemonrizetweaks.jumpPCBox",
    key(GLFW.GLFW_KEY_LEFT_ALT),
    "key.categories.cobblemonrizetweaks"
) {
    var isHeld = false

    override fun onPress() { isHeld = true; }
    override fun onRelease() { isHeld = false }
}