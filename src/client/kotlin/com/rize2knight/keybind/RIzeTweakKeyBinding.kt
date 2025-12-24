package com.rize2knight.keybind

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft

abstract class RIzeTweakKeyBinding (
    val name: String,
    val defaultKey: InputConstants.Key,
    val category: String,
    val combo: List<InputConstants.Key> = emptyList() // multi-key combo support
) {
    companion object {
        fun key(keyCode: Int): InputConstants.Key =
            InputConstants.Type.KEYSYM.getOrCreate(keyCode)
    }

    lateinit var mapping: KeyMapping
    private var wasDown = false

    /** Called once when key is pressed */
    open fun onPress() {}

    /** Called every tick while key is held */
    open fun onHold() {}

    /** Called when key is released */
    open fun onRelease() {}

    fun handleTick() {
        val isDown = isComboDown()

        if (isDown && !wasDown) { onPress() }
        if (isDown) { onHold() }
        if (!isDown && wasDown) { onRelease() }

        wasDown = isDown
    }

    private fun isComboDown(): Boolean {
        // main key must be down
        if (!mapping.isDown) return false

        // check all combo keys
        return combo.all { it.isKeyDown() }
    }

    private fun InputConstants.Key.isKeyDown(): Boolean {
        return InputConstants.isKeyDown(Minecraft.getInstance().window.window, this.value)
    }
}