package dev.matsem.astral.playground.sketches

import dev.matsem.astral.core.tools.extensions.colorModeHsb
import dev.matsem.astral.core.tools.extensions.drawShape
import dev.matsem.astral.core.tools.kontrol.KontrolF1
import org.koin.core.KoinComponent
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector

/**
 * Thing from http://piterpasma.nl/articles/rotating
 */
class PlaygroundSketch : PApplet(), KoinComponent {

    companion object {
        const val N = 4096
    }

    private val kontrol: KontrolF1 by inject()

    fun circle(phi: Float, r: Float) = PVector(r * cos(phi), r * sin(phi))
    fun R(f: Float, p: Float, a: Float, s: Float) = circle((s * f + p) * PConstants.TAU, a)
    fun O(f: Float, p: Float, v: Float, d: Float, s: Float) = v + d * sin((s * f + p) * PConstants.TAU)

    fun f(s: Float, t: Float) = PVector.add(
        R(5f, O(7f, 0f, 0.25f, .2f, .3f), .7f, s),
        R(-2f, 0f, O(7f, t, 1f, .5f, s), s)
    )

    override fun settings() {
        size(720, 720, PConstants.P2D)
    }

    override fun setup() {
        colorModeHsb()
        kontrol.connect()
    }

    override fun draw() {
        val t = millis() * 0.0002f
        background(100f, 0f, 10f)
        stroke(0f, 0f, 100f)
        noFill()
        strokeWeight(2f)

        drawShape(PConstants.CLOSE) {
            for (i in 0 until N) {
                val s = i.toFloat() / N
                val p = f(s, t)
                vertex(width / 2f + 100 * p.x, width / 2f + 100 * p.y)
            }
        }
    }
}