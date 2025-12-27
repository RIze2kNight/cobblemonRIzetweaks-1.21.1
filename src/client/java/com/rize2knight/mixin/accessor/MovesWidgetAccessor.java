package com.rize2knight.mixin.accessor;

import com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves.MoveDescriptionScrollList;
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves.MovesWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MovesWidget.class, priority = 1001)
public interface MovesWidgetAccessor {
    @Accessor(value = "descriptionScrollList", remap = false)
    MoveDescriptionScrollList getDescriptionScrollList();
}
