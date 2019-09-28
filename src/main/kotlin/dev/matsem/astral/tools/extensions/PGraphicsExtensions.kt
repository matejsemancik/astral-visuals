package dev.matsem.astral.tools.extensions

import processing.core.PGraphics
import kotlin.math.max
import kotlin.math.min

fun PGraphics.shorterDimension(): Int = min(width, height)

fun PGraphics.longerDimension(): Int = max(width, height)

fun PGraphics.centerX() = this.width / 2f

fun PGraphics.centerY() = this.height / 2f

fun PGraphics.translateCenter() = translate(centerX(), centerY())