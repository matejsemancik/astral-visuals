package dev.matsem.astral.core.tools.extensions

import dev.matsem.astral.core.ColorConfig
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

/**
 * Returns time-based radian value for given [hz] frequency at any given time (based on sketch millis()).
 * Suitable for live environments.
 */
fun PApplet.angularTimeHz(hz: Float) = millis() / 1000f * PConstants.TWO_PI / (1f / hz)

/**
 * Returns frame-based radian value for given [hz] frequency at any given frame, based on parent's [frameRate].
 * Suitable for rendering.
 */
fun PApplet.angularTimeHz(hz: Float, frameRate: Float) = PApplet.map(
    frameCount.toFloat(),
    0f,
    frameRate,
    0f,
    PConstants.TWO_PI * hz
)

fun PApplet.translate(vector: PVector) = translate(vector.x, vector.y, vector.z)

fun PApplet.rotate(vector: PVector) {
    rotateX(vector.x)
    rotateY(vector.y)
    rotateZ(vector.z)
}

fun PApplet.pushPop(block: PApplet.() -> Unit) {
    push()
    this.block()
    pop()
}

fun PApplet.drawShape(closeMode: Int, block: PApplet.() -> Unit) {
    beginShape()
    this.block()
    endShape(closeMode)
}

fun PApplet.colorModeHsb() = colorMode(
    PConstants.HSB,
    ColorConfig.HUE_MAX,
    ColorConfig.SATURATION_MAX,
    ColorConfig.BRIGHTNESS_MAX,
    ColorConfig.ALPHA_MAX
)