package sketches.workshop

import ddf.minim.AudioInput
import ddf.minim.Minim
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector

class WorkshopSketch : PApplet() {

    lateinit var minim: Minim
    lateinit var audioInput: AudioInput

    companion object {
        const val DOT_DIAMETER = 15f
        const val DOT_SPACING = 20
        const val BASE_FADE_DISTANCE = 500f
    }

    override fun settings() {
        size(1200, 600, PConstants.P2D)
        pixelDensity(2)
    }

    override fun setup() {
        minim = Minim(this)
        audioInput = minim.getLineIn()

        noCursor()
    }

    override fun draw() {
        background(30f)

        noStroke()
        fill(79f, 229f, 225f)

        val mouseVector = PVector(mouseX.toFloat(), mouseY.toFloat())

        for (x in 0..width step DOT_SPACING) {
            for (y in 0..height step DOT_SPACING) {
                val ellipseVector = PVector(x.toFloat(), y.toFloat())
                val distance = ellipseVector.dist(mouseVector)

                val range = BASE_FADE_DISTANCE * map(audioInput.mix.level(), 0f, 1f, 1f, 10f)
                var diameter = map(distance, 0f, range, DOT_DIAMETER, 0f) * map(audioInput.mix.level(), 0f, 1f, 1f, 10f)
                if (distance > range) {
                    diameter = 0f
                }

                val wiggleOffset = random(-1.5f, 1.5f) * map(diameter, 0f, DOT_DIAMETER, 0f, 1f)
                ellipse(x.toFloat() + wiggleOffset, y.toFloat(), diameter, diameter)
            }
        }
    }

    fun poisson(lambda: Double): Int {
        val L = Math.exp(-lambda)
        var p = 1.toDouble()
        var k = -1

        do {
            ++k
            p *= Math.random()
        } while (p > L)

        return k
    }
}