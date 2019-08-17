package dev.matsem.astral.tools.extensions

import dev.matsem.astral.sketches.BaseSketch
import processing.core.PConstants

fun BaseSketch.centerX() = this.width / 2f

fun BaseSketch.centerY() = this.height / 2f

fun BaseSketch.shorterDimension(): Int {
    return if (width < height) {
        width
    } else {
        height
    }
}

fun BaseSketch.longerDimension(): Int {
    return if (width > height) {
        width
    } else {
        height
    }
}

fun BaseSketch.angularVelocity(seconds: Float): Float {
    return millis() / 1000f * PConstants.TWO_PI / seconds
}