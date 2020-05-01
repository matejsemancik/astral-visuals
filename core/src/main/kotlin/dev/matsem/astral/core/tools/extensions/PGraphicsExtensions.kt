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

fun PGraphics.colorModeHsb() = colorMode(
    PConstants.HSB,
    ColorConfig.HUE_MAX,
    ColorConfig.SATURATION_MAX,
    ColorConfig.BRIGHTNESS_MAX,
    ColorConfig.ALPHA_MAX
)