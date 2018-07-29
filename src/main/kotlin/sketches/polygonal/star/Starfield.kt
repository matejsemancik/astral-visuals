package sketches.polygonal.star

import centerX
import centerY
import processing.core.PApplet

class Starfield(private val sketch: PApplet, private val starsNum: Int) {

    private val stars = mutableListOf<Star>()
    private var rotation = 0f

    init {
        repeat(starsNum, { stars.add(Star(sketch)) })
    }

    fun update(speed: Int = Star.SPEED_DEFAULT) {
        for (star in stars) {
            star.update(speed)
        }
    }

    fun rotate(rotation: Float) {
        this.rotation = rotation
    }

    fun draw() {
        sketch.pushMatrix()
        sketch.translate(sketch.centerX().toFloat(), sketch.centerY().toFloat())
        sketch.rotateZ(rotation)

        for (star in stars) {
            star.draw()
        }

        sketch.popMatrix()
    }
}