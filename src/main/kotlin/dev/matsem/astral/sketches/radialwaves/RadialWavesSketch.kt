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
import processing.core.PApplet.*

class RadialWavesSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    private val kontrol: KontrolF1 by inject()
    private val audioProcessor: AudioProcessor by inject()

    var numPoints = 10f
    var diameter = 100f

    override fun setup() = Unit

    override fun draw() = with(sketch) {
        numPoints = kontrol.knob1.midiRange(0f, 100f)
        diameter = kontrol.knob2.midiRange(100f, shorterDimension().toFloat())
        val circleNum = kontrol.knob3.midiRange(3f, 100f).toInt()
        val strokeWeight = kontrol.knob4.midiRange(1f, 10f)

        background(0)
        translateCenter()
        rotateZ(angularTimeS(60f))

        for (i in 0 until circleNum) {
            drawCircle(
                    i = i,
                    range = map(i.toFloat(), 0f, circleNum.toFloat(), 0f, 16000f).let { (it..it + 10f) },
                    audioGain = map(i.toFloat(), 0f, circleNum.toFloat(), 3f, kontrol.slider4.midiRange(-5f, -50f)),
                    color = fgColor,
                    strokeWeight = strokeWeight
            )
        }
    }

    private fun drawCircle(
            i: Int,
            range: ClosedFloatingPointRange<Float>,
            color: Int,
            strokeWeight: Float,
            audioGain: Float
    ) = with(sketch) {
        strokeWeight(strokeWeight)
        stroke(color)
        noFill()

        val phiStep = TWO_PI / numPoints
        val numDots = (TWO_PI / phiStep).toInt()

        beginShape()
        for (phi in 0 until numDots) {
            val noise = noise(phi.toFloat() + i * 2f)
            var d = diameter * (1 + noise / 10f) + audioProcessor.getRange(range).times(audioGain) * noise + i * 10f
            val x = d * cos(phi.toFloat() * phiStep)
            val y = d * sin(phi.toFloat() * phiStep)

            curveVertex(x, y)
        }
        endShape(CLOSE)
    }
}