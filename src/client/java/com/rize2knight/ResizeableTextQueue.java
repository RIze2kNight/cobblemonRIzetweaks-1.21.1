package com.rize2knight;

import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.function.Consumer;

public interface ResizeableTextQueue {

    void cobblemon_ui_tweaks$subscribe(Consumer<Component> listener);
    void cobblemon_ui_tweaks$add(Collection<Component> components);

}
