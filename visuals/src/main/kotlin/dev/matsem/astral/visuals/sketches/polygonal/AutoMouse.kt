package dev.matsem.astral.visuals.sketches.polygonal

import processing.core.PApplet
import processing.core.PVector

class AutoMouse(
    private val sketch: PApplet,
    private val centerX: Float,
    private val centerY: Float
) {

    private val baseVector: PVector = PVector(100f, 0f)
    private var multiplier = 1f

    var xPos = baseVector.x + centerX
    var yPos = baseVector.y + centerY

    fun setMultiplier(multiplier: Float) {
        this.multiplier = multiplier
    }

    fun move() {
        baseVector.rotate(0.01f)

        val drawVector = PVector(baseVector.x, baseVector.y)
        drawVector.mult(multiplier)

        xPos = centerX + drawVector.x
        yPos = centerY + drawVector.y
    }

    fun draw() {
        sketch.noStroke()
        sketch.fill(sketch.color(255, 0, 0))
        sketch.ellipse(xPos, yPos, 5f, 5f)
    }
}