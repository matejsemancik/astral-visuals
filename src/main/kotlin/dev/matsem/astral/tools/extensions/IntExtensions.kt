package dev.matsem.astral.tools.extensions

import processing.core.PApplet

fun Int.midiRange(start: Float, end: Float): Float {
    return PApplet.map(this.toFloat(), 0f, 127f, start, end)
}

fun Int.midiRange(top: Float): Float {
    return this.midiRange(0f, top)
}

fun Int.remap(start1: Float, end1: Float, start2: Float, end2: Float): Float =
        PApplet.map(this.toFloat(), start1, end1, start2, end2)