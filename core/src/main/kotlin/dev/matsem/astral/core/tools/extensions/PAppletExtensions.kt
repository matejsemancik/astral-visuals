package dev.matsem.astral.core.tools.extensions

import processing.core.PApplet
import processing.core.PApplet.max
import processing.core.PApplet.min
import processing.core.PConstants
import processing.core.PVector

fun PApplet.centerX() = this.width / 2f

fun PApplet.centerY() = this.height / 2f

fun PApplet.translateCenter() = translate(centerX(), centerY())

fun PApplet.shorterDimension(): Int = min(width, height)

fun PApplet.longerDimension(): Int = max(width, height)

/**
 * Generates saw signal with given frequency in range from 0f to 1f
 */
fun PApplet.saw(fHz: Float, offset: Int = 0): Float = ((millis() - offset) % (1000f * 1 / fHz)) / (1000f * 1 / fHz)

fun PApplet.angularTimeS(periodSeconds: Float) = millis() / 1000f * PConstants.TWO_PI / periodSeconds

fun PApplet.angularTimeHz(hz: Float) = millis() / 1000f * PConstants.TWO_PI / (1f / hz)

fun PApplet.translate(vector: PVector) = translate(vector.x, vector.y, vector.z)

fun PApplet.rotate(vector: PVector) {
    rotateX(vector.x)
    rotateY(vector.y)
    rotateZ(vector.z)
}