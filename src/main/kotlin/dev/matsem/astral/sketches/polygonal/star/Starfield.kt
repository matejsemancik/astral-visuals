package dev.matsem.astral.sketches.polygonal.star

import dev.matsem.astral.centerX
import dev.matsem.astral.centerY
import processing.core.PApplet
import processing.core.PVector

class Starfield(private val sketch: PApplet, initialCount: Int) {

    enum class Motion {
        ZOOMING,
        TRANSLATING_FORWARD,
        TRANSLATING_BACKWARD
    }

    var motion = Motion.ZOOMING
    private val stars = mutableListOf<Star>()
    private var rotation = 0f
    private var desiredCount = initialCount
    private var color = PVector(255f, 255f, 255f)

    init {
        repeat(initialCount, { addStar() })
    }

    private fun addStar() {
        stars.add(Star(sketch))
    }

    fun update(speed: Int = Star.SPEED_DEFAULT) {
        stars.removeIf { star: Star -> star.update(speed) && stars.size > desiredCount }
    }

    fun setCount(count: Int) {
        if (stars.size < count) {
            repeat(count - stars.size, { stars.add(Star(sketch)) })
        }

        desiredCount = count
    }

    fun rotate(rotation: Float) {
        this.rotation = rotation
    }

    fun setColor(a: Float, b: Float, c: Float) {
        color = PVector(a, b, c)
    }

    fun draw() {
        sketch.pushMatrix()
        sketch.translate(sketch.centerX(), sketch.centerY(), -sketch.height.toFloat())
        sketch.rotateZ(rotation)

        for (star in stars) {
            star.setColor(color.x, color.y, color.z)
            star.motion = this.motion
            star.draw()
        }

        sketch.popMatrix()
    }
}