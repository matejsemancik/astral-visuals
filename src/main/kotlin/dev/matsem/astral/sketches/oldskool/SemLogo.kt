package dev.matsem.astral.sketches.oldskool

import dev.matsem.astral.tools.extensions.rotate
import dev.matsem.astral.tools.extensions.translate
import dev.matsem.astral.tools.shapes.ExtrusionCache
import processing.core.PApplet
import processing.core.PVector

class SemLogo(
        private val cache: ExtrusionCache,
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
        scale(size / 100f)

        for (shape in cache.semLogo) {
            fillColor?.let {
                shape.setFill(true)
                shape.setFill(it)
            } ?: shape.setFill(false)

            strokeColor?.let {
                shape.setStroke(true)
                shape.setStroke(it)
                shape.setStrokeWeight(strokeWeight)
            } ?: shape.setStroke(false)

            shape(shape)
        }

        popMatrix()
    }
}