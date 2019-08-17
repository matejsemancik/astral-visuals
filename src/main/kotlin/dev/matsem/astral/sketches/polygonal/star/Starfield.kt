package dev.matsem.astral.sketches.polygonal.star

import dev.matsem.astral.tools.extensions.centerX
import dev.matsem.astral.tools.extensions.centerY
import processing.core.PApplet

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
    var color = 0

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

    @Deprecated(message = "use color property access")
    fun setColor(a: Float, b: Float, c: Float) {
        color = sketch.color(a, b, c)
    }

    fun draw() {
        sketch.pushMatrix()
        sketch.translate(sketch.centerX(), sketch.centerY(), -sketch.height.toFloat())
        sketch.rotateZ(rotation)

        for (star in stars) {
            star.color = color
            star.motion = this.motion
            star.draw()
        }

        sketch.popMatrix()
    }
}