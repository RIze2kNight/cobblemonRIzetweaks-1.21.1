package com.rize2knight.util

import net.minecraft.network.chat.Component

object BattleMessageQueue {
    private val listeners: MutableList<(Component) -> Unit> = mutableListOf()
    private val battleMessages = mutableListOf<Component>()

    // Method to add messages to the queue
    @JvmStatic
    fun add(messages: Collection<Component>) {
        battleMessages.addAll(messages)
        listeners.forEach { listener ->
            messages.forEach { message ->
                listener(message) }
        }
    }

    // Method to subscribe to the queue
    @JvmStatic
    fun subscribe(listener: (Component) -> Unit) {
        listeners.add(listener)
        battleMessages.forEach(listener)
    }

    fun clearBattleMessages(){
        battleMessages.clear()
    }
}