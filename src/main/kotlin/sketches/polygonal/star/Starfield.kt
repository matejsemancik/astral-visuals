package sketches.polygonal.star

import centerX
import centerY
import processing.core.PApplet

class Starfield(private val sketch: PApplet, initialCount: Int) {

    private val stars = mutableListOf<Star>()
    private var rotation = 0f
    private var desiredCount = initialCount

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

    fun draw() {
        sketch.pushMatrix()
        sketch.translate(sketch.centerX().toFloat(), sketch.centerY().toFloat(), -sketch.height.toFloat())
        sketch.rotateZ(rotation)

        for (star in stars) {
            star.draw()
        }

        sketch.popMatrix()
    }
}