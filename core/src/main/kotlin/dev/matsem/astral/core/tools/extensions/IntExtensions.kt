package dev.matsem.astral.core.tools.extensions

import processing.core.PApplet

fun Int.midiRange(start: Float, end: Float): Float {
    return PApplet.map(this.toFloat(), 0f, 127f, start, end)
}

fun Int.midiRange(top: Float): Float {
    return this.midiRange(0f, top)
}

fun Int.remap(start1: Float, end1: Float, start2: Float, end2: Float): Float =
    PApplet.map(this.toFloat(), start1, end1, start2, end2)

fun Int.constrain(low: Int = Int.MIN_VALUE, high: Int = Int.MAX_VALUE): Int = PApplet.constrain(this, low, high)

fun Int.toMidi(low: Int, high: Int): Int = PApplet.map(this.toFloat(), low.toFloat(), high.toFloat(), 0f, 127f).toInt()