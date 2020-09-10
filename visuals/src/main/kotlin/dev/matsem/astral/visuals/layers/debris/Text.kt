package dev.matsem.astral.visuals.layers.debris

import dev.matsem.astral.core.tools.extensions.pushPop
import dev.matsem.astral.core.tools.extensions.rotate
import dev.matsem.astral.core.tools.extensions.translate
import dev.matsem.astral.core.tools.shapes.ExtrusionCache
import processing.core.PGraphics
import processing.core.PVector

class Text(
    private val text: String,
    private val cache: ExtrusionCache,
    position: PVector,
    rotation: PVector,
    rotationVector: PVector,
    size: Float,
    targetSize: Float
) : FlyingObject(position, rotation, rotationVector, size, targetSize) {

    override fun draw(canvas: PGraphics) = with(canvas) {
        pushPop {
            translate(position)
            rotate(rotation)
            scale(size / 48f)

            for (shape in cache.getText(text)) {
                shape.disableStyle()
                shape.setStroke(true)
                shape(shape)
            }
        }
    }
}