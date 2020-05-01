package dev.matsem.astral.visuals.sketches.oldskool

import dev.matsem.astral.core.tools.extensions.rotate
import dev.matsem.astral.core.tools.extensions.translate
import processing.core.PApplet
import processing.core.PVector

class Box(
    position: PVector,
    rotation: PVector,
    rotationVector: PVector,
    size: Float,
    targetSize: Float
) : FlyingObject(position, rotation, rotationVector, size, targetSize) {

    override fun draw(sketch: PApplet) = with(sketch) {
        pushMatrix()
        translate(position)
        rotate(rotation)
        box(size)
        popMatrix()
    }
}