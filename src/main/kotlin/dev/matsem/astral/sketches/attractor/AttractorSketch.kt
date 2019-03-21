package dev.matsem.astral.sketches.attractor

import dev.matsem.astral.*
import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.galaxy.Galaxy
import org.koin.core.inject
import processing.core.PVector

class AttractorSketch : BaseSketch() {

    companion object {
        const val ITERATIONS_MAX = 50000
    }

    override val sketch: SketchLoader by inject()
    val audioProcessor: AudioProcessor by inject()
    val galaxy: Galaxy by inject()

    private var deJongRight = Float.MIN_VALUE
    private var deJongLeft = Float.MAX_VALUE
    private var deJongTop = Float.MAX_VALUE
    private var deJongBottom = Float.MIN_VALUE
    private val deJongPoints = Array(ITERATIONS_MAX) { PVector() }

    private val iterationCountSlider = galaxy.createPot(5, 0, 100f, ITERATIONS_MAX.toFloat(), 25000f)
    private val scaleSlider = galaxy.createPot(5, 1, 1f, 600f, 250f)
    private val sliderA = galaxy.createPot(5, 2, -10f, 10f, 4f)
    private val sliderB = galaxy.createPot(5, 3, 0f, 1f, 0.54f)
    private val sliderC = galaxy.createPot(5, 4, -0.5f, 0.5f, 0.40f)
    private val sliderD = galaxy.createPot(5, 5, -10f, 10f, -2.43f)
    private val stretchSlider = galaxy.createPot(5, 6, 0.5f, 2f, 1f)
    private val stabilizeBtn = galaxy.createToggleButton(5, 7, true)
    private val automateBtn = galaxy.createToggleButton(5, 8, false)

    override fun setup() = Unit

    override fun draw() {
        background(bgColor)

        if (automateBtn.isPressed) {
            sliderA.sendValue(millis() / 1500f % 20f - 10f + audioProcessor.getRange(0f..100f) / 100f)
            if (audioProcessor.beatDetect.isKick) {
                sliderC.random()
            }
            sliderB.sendValue(sin(angularVelocity(6f)) mapFrom (-1f..1f) to (sliderB.min..sliderB.max))
            sliderC.sendValue(0.40f + audioProcessor.getRange(200f..600f) / 1000f)
            sliderD.sendValue(millis() / 1000f % 20f - 10f + audioProcessor.getRange(800f..1200f) / 100f)
        }

        deJongAttractor(sliderA.value, sliderB.value, sliderC.value, sliderD.value)

        deJongPoints
                .map { PVector(it.x * scaleSlider.value, it.y * scaleSlider.value) }
                .forEach {
                    if (it.x > deJongRight) deJongRight = it.x
                    if (it.x < deJongLeft) deJongLeft = it.x
                    if (it.y < deJongTop) deJongTop = it.y
                    if (it.y > deJongBottom) deJongBottom = it.y
                }

        val deJongWidth = deJongRight - deJongLeft
        val deJongHeight = deJongBottom - deJongTop

        translate(centerX(), centerY())
        stroke(fgColor)
        fill(fgColor)

        // DEBUG
        if (isInDebugMode) {
            sketch.line(deJongRight, -height / 8f, deJongRight, height / 8f)
            sketch.line(deJongLeft, -height / 8f, deJongLeft, height / 8f)
            sketch.line(-width / 8f, deJongTop, width / 8f, deJongTop)
            sketch.line(-width / 8f, deJongBottom, width / 8f, deJongBottom)
        }

        for (i in 0 until iterationCountSlider.value.toInt()) {
            val pt = deJongPoints[i]
            if (stabilizeBtn.isPressed) {
                point(
                        pt.x * scaleSlider.value
                                mapFrom (deJongLeft..deJongRight)
                                to (-deJongWidth / 2f * stretchSlider.value..deJongWidth / 2f * stretchSlider.value),
                        pt.y * scaleSlider.value
                                mapFrom (deJongTop..deJongBottom)
                                to (-deJongHeight / 2f..deJongHeight / 2f)
                )
            } else {
                point(pt.x * scaleSlider.value, pt.y * scaleSlider.value)
            }
        }

        deJongRight = Float.MIN_VALUE
        deJongLeft = Float.MAX_VALUE
        deJongTop = Float.MAX_VALUE
        deJongBottom = Float.MIN_VALUE
    }

    private fun deJongAttractor(a: Float, b: Float, c: Float, d: Float) {
        var px = 0f
        var py = 0f

        for (i in 0 until iterationCountSlider.value.toInt()) {
            val x = sin(a * py) - cos(b * px)
            val y = sin(c * px) - cos(d * py)

            px = x
            py = y

            deJongPoints[i] = PVector(x, y)
        }
    }
}