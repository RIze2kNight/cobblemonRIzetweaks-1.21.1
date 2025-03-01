package com.rize2knight.mixin.battle.log;

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler;
import com.cobblemon.mod.common.client.net.battle.BattleMessageHandler;
import com.cobblemon.mod.common.net.messages.client.battle.BattleMessagePacket;
import com.rize2knight.util.BattleMessageQueue;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BattleMessageHandler.class, priority = 1001)
public abstract class BattleMessageHandlerMixin implements ClientNetworkPacketHandler<BattleMessagePacket> {

    @Inject(method = "handle*", at = @At("TAIL"))
    public void handle(@NotNull BattleMessagePacket battleMessagePacket, @NotNull Minecraft minecraft, CallbackInfo ci) {
        BattleMessageQueue.add(battleMessagePacket.getMessages());
    }

}
