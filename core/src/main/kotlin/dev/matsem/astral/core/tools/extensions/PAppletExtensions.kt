package dev.matsem.astral.core.tools.extensions

import dev.matsem.astral.core.ColorConfig
import processing.core.PApplet
import processing.core.PApplet.max
import processing.core.PApplet.min
import processing.core.PConstants
import processing.core.PVector

/**
 * Returns horizontal center of this [PApplet]'s window in pixels.
 */
fun PApplet.centerX() = this.width / 2f

/**
 * Returns vertical center of this [PApplet]'s window in pixels.
 */
fun PApplet.centerY() = this.height / 2f

/**
 * Translates the current transformation matrix to the center
 * of this [PApplet]'s window in 2D space (does not translate in Z dimension).
 */
fun PApplet.translateCenter() = translate(centerX(), centerY())

/**
 * Returns the smaller pixel dimension of this [PApplet]'s window.
 */
fun PApplet.shorterDimension(): Int = min(width, height)

/**
 * Returns the bigger pixel dimension of this [PApplet]'s window.
 */
fun PApplet.longerDimension(): Int = max(width, height)

/**
 * Generates saw signal with given frequency in range from 0f to 1f
 */
@Deprecated("Use AnimationHandler to generate the signal")
fun PApplet.saw(fHz: Float, offset: Int = 0): Float = ((millis() - offset) % (1000f * 1 / fHz)) / (1000f * 1 / fHz)

@Deprecated("Use AnimationHandler to generate the value")
fun PApplet.angularTimeS(periodSeconds: Float) = millis() / 1000f * PConstants.TWO_PI / periodSeconds

@Deprecated("Use AnimationHandler to generate the value")
fun PApplet.angularTimeHz(hz: Float) = millis() / 1000f * PConstants.TWO_PI / (1f / hz)

/**
 * Translates the current transformation matrix according to offsets passed in [vector].
 */
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

fun PApplet.drawShape(kind: Int? = null, closeMode: Int? = null, block: PApplet.() -> Unit) {
    kind?.let { beginShape(it) } ?: beginShape()
    this.block()
    closeMode?.let { endShape(it) } ?: endShape()
}

fun PApplet.colorModeHsb() = colorMode(
    PConstants.HSB,
    ColorConfig.HUE_MAX,
    ColorConfig.SATURATION_MAX,
    ColorConfig.BRIGHTNESS_MAX,
    ColorConfig.ALPHA_MAX
)