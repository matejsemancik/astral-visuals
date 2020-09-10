package dev.matsem.astral.visuals.layers.debris

import dev.matsem.astral.core.tools.extensions.plusAssign
import processing.core.PApplet
import processing.core.PGraphics
import processing.core.PVector

abstract class FlyingObject(
    var position: PVector,
    var rotation: PVector,
    var rotationVector: PVector,
    var size: Float,
    var targetSize: Float
) {
    fun update(speed: Float) {
        position += PVector(0f, 0f, speed)
        rotation += rotationVector
        size = PApplet.lerp(size, targetSize, 0.1f)
    }

    abstract fun draw(canvas: PGraphics)
}