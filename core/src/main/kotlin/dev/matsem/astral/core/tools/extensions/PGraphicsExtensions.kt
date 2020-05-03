package dev.matsem.astral.core.tools.extensions

import dev.matsem.astral.core.ColorConfig
import processing.core.PApplet.max
import processing.core.PApplet.min
import processing.core.PConstants
import processing.core.PGraphics

fun PGraphics.shorterDimension(): Int = min(width, height)

fun PGraphics.longerDimension(): Int = max(width, height)

fun PGraphics.centerX() = this.width / 2f

fun PGraphics.centerY() = this.height / 2f

fun PGraphics.translateCenter() = translate(centerX(), centerY())

fun PGraphics.pushPop(block: PGraphics.() -> Unit) {
    push()
    this.block()
    pop()
}

fun PGraphics.draw(block: PGraphics.() -> Unit) {
    beginDraw()
    this.block()
    endDraw()
}

/**
 * Fades whole image to complete black by [fadeAmount] in range 0f..1f
 */
fun PGraphics.fadeToBlackBy(fadeAmount: Float) {
    loadPixels()
    for (i in 0 until pixels.count()) {
        val r = pixels[i].rgbRed
        val newR = kotlin.math.max(0, (r * fadeAmount).toInt())
        val g = pixels[i].rgbGreen
        val newG = kotlin.math.max(0, (g * fadeAmount).toInt())
        val b = pixels[i].rgbBlue
        val newB = kotlin.math.max(0, (b * fadeAmount).toInt())
        pixels[i] = (0xff shl 24) or (newR shl 16) or (newG shl 8) or newB
    }
    updatePixels()
}

fun PGraphics.colorModeHsb() = colorMode(
    PConstants.HSB,
    ColorConfig.HUE_MAX,
    ColorConfig.SATURATION_MAX,
    ColorConfig.BRIGHTNESS_MAX,
    ColorConfig.ALPHA_MAX
)