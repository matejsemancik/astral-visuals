package dev.matsem.astral.tools.extensions

import processing.core.PApplet

fun PApplet.centerX() = this.width / 2f

fun PApplet.centerY() = this.height / 2f

/**
 * Generates saw signal with given frequency in range from 0f to 1f
 */
fun PApplet.saw(fHz: Float): Float = (millis() % (1000f * 1 / fHz)) / (1000f * 1 / fHz)

fun PApplet.translateCenter() = translate(centerX(), centerY())