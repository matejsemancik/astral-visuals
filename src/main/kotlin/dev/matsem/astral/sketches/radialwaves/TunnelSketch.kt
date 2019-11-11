package dev.matsem.astral.sketches.radialwaves

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.extensions.centerX
import dev.matsem.astral.tools.extensions.centerY
import dev.matsem.astral.tools.extensions.midiRange
import dev.matsem.astral.tools.extensions.shorterDimension
import dev.matsem.astral.tools.kontrol.KontrolF1
import org.koin.core.inject
import processing.core.PApplet.cos
import processing.core.PApplet.sin
import processing.core.PConstants.CLOSE
import processing.core.PConstants.TWO_PI

class TunnelSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    private val kontrol: KontrolF1 by inject()
    private val audioProcessor: AudioProcessor by inject()

    override fun onBecameActive() = Unit

    override fun setup() = Unit

    override fun draw() = with(sketch) {
        val strkWeight = kontrol.knob1.midiRange(1f, 20f)
        val numPoints = kontrol.knob2.midiRange(3f, 12f).toInt()

        background(bgColor)
        noFill()
        stroke(fgColor)

        translate(centerX(), centerY(), 0f)
        polygon(shorterDimension() / 2f, numPoints)
    }

    private fun polygon(radius: Float, nPoints: Int) = with(sketch) {
        val angle = TWO_PI / nPoints
        beginShape()
        var a = 0f
        while (a < TWO_PI) {
            val sx = cos(a) * radius
            val sy = sin(a) * radius
            vertex(sx, sy)
            a += angle
        }
        endShape(CLOSE)
    }
}