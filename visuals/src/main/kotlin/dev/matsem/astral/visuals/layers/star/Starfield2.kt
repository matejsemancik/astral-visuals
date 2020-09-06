package dev.matsem.astral.visuals.layers.star

import dev.matsem.astral.core.tools.extensions.centerX
import dev.matsem.astral.core.tools.extensions.centerY
import dev.matsem.astral.core.tools.extensions.pushPop
import processing.core.PApplet
import processing.core.PGraphics

class Starfield2(private val parent: PApplet, private val canvas: PGraphics, initialCount: Int) {

    enum class Motion {
        ZOOMING,
        TRANSLATING_FORWARD,
        TRANSLATING_BACKWARD
    }

    var motion = Motion.ZOOMING
    var mode = 0
    private val stars = mutableListOf<Star2>()
    private var rotation = 0f
    private var desiredCount = initialCount
    private var color = 0

    init {
        repeat(initialCount, { addStar() })
    }

    private fun addStar() {
        stars.add(Star2(parent, canvas))
    }

    fun update(speed: Int = Star2.SPEED_DEFAULT) {
        stars.removeIf { star: Star2 -> star.update(speed) && stars.size > desiredCount }
    }

    fun setCount(count: Int) {
        if (stars.size < count) {
            repeat(count - stars.size) { stars.add(Star2(parent, canvas)) }
        }

        desiredCount = count
    }

    fun rotate(rotation: Float) {
        this.rotation = rotation
    }

    fun setColor(rgb: Int) {
        color = rgb
    }

    fun draw() = with(canvas) {
        pushPop {
            translate(centerX(), centerY(), -height.toFloat())
            rotateZ(rotation)

            for (star in stars) {
                star.setColor(color)
                star.motion = this@Starfield2.motion
                star.mode = this@Starfield2.mode
                star.draw()
            }
        }
    }
}