package dev.matsem.astral.core.tools.extensions

import processing.core.PShape

/**
 * PShape.rotateZ bug workaround, https://github.com/processing/processing/issues/5770
 */
fun PShape.rotate(angleX: Float, angleY: Float, angleZ: Float) {
    rotateX(angleX)
    rotateY(angleY)
    rotate(angleZ, 0f, 0f, 1f)
}