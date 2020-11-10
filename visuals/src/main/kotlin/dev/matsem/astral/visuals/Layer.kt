package dev.matsem.astral.visuals

import processing.core.PApplet
import processing.core.PConstants
import processing.core.PGraphics

abstract class Layer {

    abstract val parent: PApplet
    abstract fun PGraphics.draw()

    open val renderer = PConstants.P3D
    val canvas: PGraphics by lazy { parent.createGraphics(parent.width, parent.height, renderer) }

    fun update() {
        canvas.beginDraw()
        canvas.draw()
        canvas.endDraw()
    }
}