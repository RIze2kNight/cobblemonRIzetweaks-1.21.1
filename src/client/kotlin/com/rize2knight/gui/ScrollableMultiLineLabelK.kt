package com.rize2knight.gui

import com.cobblemon.mod.common.api.gui.drawString
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.util.Mth

open class ScrollableMultiLineLabelK(
    component: Component,
    private val ySpacing: Number,
    private val width: Double,
    private val height: Double,
    private val maxLines: Int,
)
{
    private var x: Double = 0.0
    private var y: Double = 0.0
    private val font = Minecraft.getInstance().font
    private var scrollAmount = 0.0          // actual rendered offset
    private var targetScroll = 0.0          // where scrolling wants to go
    private val smoothness = 0.25           // higher is smoother but slower

    // Helper functions to get boundaries
    private fun top(): Int = (x + width + 5).toInt()
    private fun bottom(): Int = (y + height).toInt()

    // Hover states
    var isHovered = false; private set
    var isScrollBarHovered = false; private set

    // Split the component into lines based on the specified width
    private val lines: List<String> =
        font.splitter
            .splitLines(component, width.toInt(), net.minecraft.network.chat.Style.EMPTY)
            .map { it.string }

    // Pre-calculated dimensions
    private val lineHeight = font.lineHeight.toDouble()
    private val lineSpacing = 2.0
    private val visibleHeight: Double get() = (maxLines * lineHeight) + ((maxLines - 1) * lineSpacing)
    private val contentHeight: Int get() = ((lines.size * lineHeight) + (lines.size - 1) * lineSpacing).toInt()
    private val maxScroll: Double get() = maxOf(0.0, contentHeight - visibleHeight)

    // Handle mouse scroll input
    fun mouseScrolled(amount: Double) {
        targetScroll = Mth.clamp(
            targetScroll - amount * lineHeight * 1.5,
            0.0,
            maxScroll
        )
    }

    // Handle mouse drag input
    fun mouseDragged(button: Int, dragY: Double) {
        if(hasOverflow() && button == 0 && isScrollBarHovered) {
            val barHeight = scrollbarBarHeight()
            val trackHeight = height - barHeight

            val delta = dragY / trackHeight * maxScroll
            targetScroll = Mth.clamp(
                    targetScroll + delta,
                    0.0,
                    maxScroll
            )
        }
    }

    // Smoothly interpolate scroll amount towards target scroll
    private fun tickScroll() {
        scrollAmount += (targetScroll - scrollAmount) * smoothness

        if (kotlin.math.abs(targetScroll - scrollAmount) < 0.1) {
            scrollAmount = targetScroll
        }
    }

    fun render(
        context: GuiGraphics,
        x: Double, y: Double,
        pMouseX: Float, pMouseY: Float,
        colour: Int,
        shadow: Boolean = true,
    ) {
        // Scale factor for rendering
        val scale = 1F
        val matrices = context.pose()
        matrices.pushPose()
        matrices.scale(scale, scale, 1f)

        this.x = x
        this.y = y
        val smallTextScale = 0.5F

        tickScroll()

        // --- CLIPPING ---
        context.enableScissor(
            (x * smallTextScale).toInt(),
            (y * smallTextScale).toInt(),
            ((x + width)*smallTextScale).toInt(),
            ((y + visibleHeight)*smallTextScale).toInt()
        )

        lines.forEachIndexed {index, line ->
            val lineY = y + (index * ySpacing.toDouble()) - scrollAmount

            if (lineY + lineHeight >= y && lineY <= y + visibleHeight) {
                drawString(
                    context = context,
                    x = x.toFloat(),
                    y = lineY/scale,
                    colour = colour,
                    shadow = shadow,
                    text = line,
                    font = null
                )
            }
        }

        context.disableScissor()
        updateHover(pMouseX, pMouseY)
        matrices.popPose()

        if (hasOverflow()) {
            renderScrollbar(context, y)
        }
    }

    private fun renderScrollbar(context: GuiGraphics, y: Double) {
        val barHeight = scrollbarBarHeight()
        val trackHeight = height - barHeight + 8

        val barY = Mth.clamp(
            (y - 8) + (scrollAmount / maxScroll) * trackHeight,
            y - 8,
            y - 8 + trackHeight
        )
        val xLeft = top()

        // Track
        context.fill(
            xLeft,
            (y - 8).toInt(),
            xLeft + 3,
            (y + height).toInt(),
            0xAA000000.toInt()
        )

        // Bar
        context.fill(
            xLeft,
            barY.toInt(),
            xLeft + 3,
            (barY + barHeight).toInt(),
            0xFFE6E6E6.toInt()
        )
    }

    fun updateHover(pMouseX: Float, pMouseY: Float) {
        // Update hover state
        this.isHovered = pMouseX >= x &&
                         pMouseY >= y - 8 &&
                         pMouseX < top() + 3 &&
                         pMouseY < bottom()

        this.isScrollBarHovered = pMouseX >= top().toDouble() - 3 &&
                                  pMouseX < (top() + 3).toDouble() + 3 &&
                                  pMouseY >= y - 10 &&
                                  pMouseY < bottom() + 2
    }

    private fun scrollbarBarHeight(): Double {
        val ratio = height / contentHeight
        return Mth.clamp(height * ratio, 16.0, height * 0.8)
    }

    // Check if there is overflow (more lines than maxLines)
    fun hasOverflow() : Boolean { return contentHeight > visibleHeight }
}
