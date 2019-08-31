package dev.matsem.astral.tools.extensions

import processing.core.PApplet
import processing.core.PConstants

fun PApplet.centerX() = this.width / 2f

fun PApplet.centerY() = this.height / 2f

/**
 * Generates saw signal with given frequency in range from 0f to 1f
 */
fun PApplet.saw(fHz: Float): Float = (millis() % (1000f * 1 / fHz)) / (1000f * 1 / fHz)

fun PApplet.translateCenter() = translate(centerX(), centerY())

fun PApplet.angularTimeS(periodSeconds: Float) = millis() / 1000f * PConstants.TWO_PI / periodSeconds

fun PApplet.angularTimeHz(hz: Float) = millis() / 1000f * PConstants.TWO_PI / (1f / hz)