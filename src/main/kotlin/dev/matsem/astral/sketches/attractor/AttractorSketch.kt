package dev.matsem.astral.sketches.attractor

import controlP5.ControlP5
import dev.matsem.astral.centerX
import dev.matsem.astral.centerY
import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import org.koin.core.inject
import processing.core.PVector

// TODO use Galaxy controls
class AttractorSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    val cp5: ControlP5 by inject()

    private var iterations = 40000
    private val deJongPoints = Array(iterations) { PVector() }

    private val iterationCountSlider = cp5.addSlider("iterations", 1000f, iterations.toFloat())
            .linebreak()
            .apply { value = iterations.toFloat() }

    private val sliderA = cp5.addSlider("a", -10f, 10f)
            .linebreak()
            .apply { value = -2.24f }

    private val sliderB = cp5.addSlider("b", 0f, 2f)
            .linebreak()
            .apply { value = 2.0f }

    private val sliderC = cp5.addSlider("c", -0.5f, 0.5f)
            .linebreak()
            .apply { value = -0.66f }

    private val sliderD = cp5.addSlider("d", -10f, 10f)
            .linebreak()
            .apply { value = -2.43f }

    override fun setup() = Unit

    override fun draw() {
        iterations = iterationCountSlider.value.toInt()
        sliderA.value = millis() / 3000f % 20f - 10f
        sliderD.value = millis() / 6000f % 20f - 10f

        deJongAttractor(sliderA.value, sliderB.value, sliderC.value, sliderD.value)

        background(bgColor)
        cp5.draw()

        translate(centerX(), centerY())
        stroke(fgColor)
        fill(fgColor)

        for (i in 0 until iterations) {
            val pt = deJongPoints[i]
            point(pt.x * 200f, pt.y * 200f)
        }
    }

    private fun deJongAttractor(a: Float, b: Float, c: Float, d: Float) {
        var px = 0f
        var py = 0f

        for (i in 0 until iterations) {
            val x = sin(a * py) - cos(b * px)
            val y = sin(c * px) - cos(d * py)

            px = x
            py = y

            deJongPoints[i] = PVector(x, y)
        }
    }
}