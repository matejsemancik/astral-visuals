package dev.matsem.astral.visuals.layers.debris

import dev.matsem.astral.core.tools.extensions.rotate
import dev.matsem.astral.core.tools.extensions.translate
import dev.matsem.astral.core.tools.shapes.ExtrusionCache
import processing.core.PGraphics
import processing.core.PVector

class SemLogo(
    private val cache: ExtrusionCache,
    position: PVector,
    rotation: PVector,
    rotationVector: PVector,
    size: Float,
    targetSize: Float
) : FlyingObject(position, rotation, rotationVector, size, targetSize) {

    override fun draw(canvas: PGraphics) = with(canvas) {
        pushMatrix()
        translate(position)
        rotate(rotation)
        scale(size / 100f)

        for (shape in cache.semLogo) {
            shape.disableStyle()
            shape(shape)
        }
        popMatrix()
    }
}