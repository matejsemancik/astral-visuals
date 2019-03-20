package dev.matsem.astral.sketches.attractor

import controlP5.ControlP5
import dev.matsem.astral.*
import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import org.koin.core.inject
import processing.core.PApplet
import processing.core.PVector

// TODO use Galaxy controls
class AttractorSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    val audioProcessor: AudioProcessor by inject()
    val cp5: ControlP5 by inject()

    enum class Attractor {
        DE_JONG, AIZAWA;

        // Cache values for better performance
        companion object {
            val VALUES = values()
        }
    }

    private var iterations = 25000
    private var deJongStabilize = true
    private var deJongRight = Float.MIN_VALUE
    private var deJongLeft = Float.MAX_VALUE
    private var deJongTop = Float.MAX_VALUE
    private var deJongBottom = Float.MIN_VALUE

    private val deJongPoints = Array(iterations) { PVector() }

    private val aizawaPoints = Array(iterations) { PVector() }

    private val buttonBar = cp5.addButtonBar("type")
            .linebreak()
            .apply {
                addItems(arrayOf("de jong", "aizawa"))
            }

    private val iterationCountSlider = cp5.addSlider("iterations", 1000f, iterations.toFloat()).linebreak().apply { value = iterations.toFloat() }
    private val deJongScaleSlider = cp5.addSlider("scale", 1f, 600f).linebreak().apply { value = 250f }
    private val sliderA = cp5.addSlider("De Jong A", -10f, 10f).linebreak().apply { value = 4f }
    private val sliderB = cp5.addSlider("De Jong B", 0f, 1f).linebreak().apply { value = 0.54f }
    private val sliderC = cp5.addSlider("De Jong C", -0.5f, 0.5f).linebreak().apply { value = 0.40f }
    private val sliderD = cp5.addSlider("De Jong D", -10f, 10f).linebreak().apply { value = -2.43f }
    private val stretchXSlider = cp5.addSlider("stretch x", 0.5f, 2f).linebreak().apply { value = 1f }

    override fun setup() = Unit

    override fun draw() {
        background(bgColor)
        cp5.draw()

        iterations = iterationCountSlider.value.toInt()

        when (Attractor.VALUES[buttonBar.value.toInt()]) {
            Attractor.DE_JONG -> {
                sliderA.value = millis() / 1500f % 20f - 10f + audioProcessor.getRange(0f..100f) / 100f
                if (audioProcessor.beatDetect.isKick) {
                    sliderC.value = sketch.random(-0.2f, 0.2f)
                }
                sliderB.value = sin(angularVelocity(6f)) mapFrom (-1f..1f) to (sliderB.min..sliderB.max)
                sliderC.value = 0.40f + audioProcessor.getRange(200f..600f) / 1000f
                sliderD.value = millis() / 1000f % 20f - 10f + audioProcessor.getRange(800f..1200f) / 100f

                deJongAttractor(sliderA.value, sliderB.value, sliderC.value, sliderD.value)

                deJongPoints
                        .map { PVector(it.x * deJongScaleSlider.value, it.y * deJongScaleSlider.value) }
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

                for (i in 0 until iterations) {
                    val pt = deJongPoints[i]
                    if (deJongStabilize) {
                        point(
                                pt.x * deJongScaleSlider.value
                                        mapFrom (deJongLeft..deJongRight)
                                        to (-deJongWidth / 2f * stretchXSlider.value..deJongWidth / 2f * stretchXSlider.value),
                                pt.y * deJongScaleSlider.value
                                        mapFrom (deJongTop..deJongBottom)
                                        to (-deJongHeight / 2f..deJongHeight / 2f)
                        )
                    } else {
                        point(pt.x * deJongScaleSlider.value, pt.y * deJongScaleSlider.value)
                    }
                }

                deJongRight = Float.MIN_VALUE
                deJongLeft = Float.MAX_VALUE
                deJongTop = Float.MAX_VALUE
                deJongBottom = Float.MIN_VALUE
            }

            Attractor.AIZAWA -> {
                aizawaAttractor()
                translate(centerX(), centerY())
                stroke(fgColor)
                strokeWeight(1f)
                fill(fgColor)
                rotateY(millis() / 4000f)

                sketch.beginShape()
                for (i in 0 until iterations) {
                    val pt = aizawaPoints[i]
                    vertex(pt.x * 200f, pt.y * 200f, pt.z * 200f)
                }
                sketch.endShape()
            }
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

    private fun aizawaAttractor(
            a: Float = 0.95f,
            b: Float = 0.7f,
            c: Float = 0.6f,
            d: Float = 3.5f,
            e: Float = 0.25f,
            f: Float = 0.1f,
            time: Float = 0.01f
    ) {
        aizawaPoints[0] = PVector(0.00001f, 0.00001f, 0.00001f)

        for (i in 1 until iterations) {
            val px = aizawaPoints[i - 1].x
            val py = aizawaPoints[i - 1].y
            val pz = aizawaPoints[i - 1].z

            val x = (pz - b) * px - d * py
            val y = d * px + (pz - b) * py
            val z = c + a * pz - (PApplet.pow(pz, 3f) / 3f) * (1f + e * pz) + f * pz * PApplet.pow(px, 3f)

            aizawaPoints[i] = PVector(px + x * time, py + y * time, pz + z * time)
        }
    }
}