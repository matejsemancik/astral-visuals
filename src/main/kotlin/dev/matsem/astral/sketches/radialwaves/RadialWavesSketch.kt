package dev.matsem.astral.sketches.radialwaves

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.extensions.angularTimeS
import dev.matsem.astral.tools.extensions.midiRange
import dev.matsem.astral.tools.extensions.shorterDimension
import dev.matsem.astral.tools.extensions.translateCenter
import dev.matsem.astral.tools.kontrol.KontrolF1
import org.koin.core.inject
import processing.core.PApplet

class RadialWavesSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    private val kontrol: KontrolF1 by inject()
    private val audioProcessor: AudioProcessor by inject()

    var numPoints = 10f
    var diameter = 100f

    override fun setup() {

    }

    override fun draw() = with(sketch) {
        numPoints = kontrol.knob1.midiRange(0f, 100f)
        diameter = kontrol.knob2.midiRange(100f, shorterDimension().toFloat())
        val rotInterval = kontrol.knob3.midiRange(1f, 10f)

        background(0)
        translateCenter()
        rotateZ(angularTimeS(rotInterval))

        drawCircle((20f..200f), color(fgHue, fgSat, fgBrightness), 1f)
        drawCircle((600f..1600f), color(fgHue, fgSat, fgBrightness), 5f)
        drawCircle((4000f..16000f), color(fgHue, fgSat, fgBrightness), 15f)
    }

    private fun drawCircle(range: ClosedFloatingPointRange<Float>, color: Int, audioGain: Float) = with(sketch) {
        strokeWeight(10f)
        stroke(color)
        noFill()

        val phiStep = PApplet.TWO_PI / numPoints
        val numDots = (PApplet.TWO_PI / phiStep).toInt()
        val d = diameter + audioProcessor.getRange(range) * audioGain

        for (phi in 0 until numDots) {
            val x = d * PApplet.cos(phi.toFloat() * phiStep)
            val y = d * PApplet.sin(phi.toFloat() * phiStep)

            point(x, y)
        }
    }
}