package dev.matsem.astral.sketches.oldskool

import dev.matsem.astral.tools.extensions.plusAssign
import processing.core.PApplet
import processing.core.PVector

abstract class FlyingObject(
        var position: PVector,
        var rotation: PVector,
        var rotationVector: PVector,
        var size: Float,
        var targetSize: Float
) {
    open var fillColor: Int? = 0
    open var strokeColor: Int? = 0
    open var strokeWeight: Float = 4f

    fun update(speed: Float) {
        position += PVector(0f, 0f, speed)
        rotation += rotationVector
        size = PApplet.lerp(size, targetSize, 0.1f)
    }

    abstract fun draw(sketch: PApplet)
}