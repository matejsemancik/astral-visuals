package dev.matsem.astral.sketches.radialwaves

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.extensions.midiRange
import dev.matsem.astral.tools.extensions.shorterDimension
import dev.matsem.astral.tools.extensions.translateCenter
import dev.matsem.astral.tools.kontrol.KontrolF1
import org.koin.core.inject
import processing.core.PApplet

class RadialWavesSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    private val kontrol: KontrolF1 by inject()

    var numPoints = 10f
    var diameter = 100f

    override fun setup() {

    }

    override fun draw() = with(sketch) {
        numPoints = kontrol.knob1.midiRange(0f, 100f)
        diameter = kontrol.knob2.midiRange(100f, shorterDimension().toFloat())

        background(0)
        translateCenter()

        strokeWeight(10f)
        stroke(fgColor)
        noFill()

        val phiStep = PApplet.TWO_PI / numPoints
        val numDots = (PApplet.TWO_PI / phiStep).toInt()

        for (phi in 0 until numDots) {
            val x = diameter * PApplet.cos(phi.toFloat() * phiStep)
            val y = diameter * PApplet.sin(phi.toFloat() * phiStep)

            point(x, y)
        }
    }
}