package dev.matsem.astral.core.tools.extensions

import processing.core.PApplet
import kotlin.math.absoluteValue

fun Float.threshold(threshold: Float): Float {
    return if (this.absoluteValue > threshold) {
        this
    } else {
        0f
    }
}

fun Float.mapp(start: Float, end: Float): Float {
    return PApplet.map(this, 0f, 1f, start, end)
}

fun Float.mapSin(start: Float, end: Float): Float {
    return PApplet.map(this, -1f, 1f, start, end)
}

fun Float.toMidi(low: Float, high: Float): Int = PApplet.map(this, low, high, 0f, 127f).toInt()

fun Float.remap(start1: Float, end1: Float, start2: Float, end2: Float): Float =
    PApplet.map(this, start1, end1, start2, end2)

fun Float.quantize(step: Float): Float = (this / step).toInt() * step

fun Float.constrain(low: Float = Float.MIN_VALUE, high: Float = Float.MAX_VALUE): Float =
    PApplet.constrain(this, low, high)