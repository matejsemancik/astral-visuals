package dev.matsem.astral.sketches.radialwaves

import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.audio.AudioProcessor
import dev.matsem.astral.tools.extensions.*
import dev.matsem.astral.tools.kontrol.KontrolF1
import org.koin.core.inject
import processing.core.PApplet.*
import processing.core.PConstants.CLOSE
import processing.core.PConstants.TWO_PI

class TunnelSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    private val kontrol: KontrolF1 by inject()
    private val audioProcessor: AudioProcessor by inject()
    private val ex: extruder.extruder by inject()

    private val hexagon = with(sketch) {
        ex.extrude(
                createShape().apply {
                    val radius = shorterDimension() / 2f
                    val angle = TWO_PI / 6

                    beginShape()
                    var a = 0f
                    while (a < TWO_PI) {
                        val sx = cos(a) * radius
                        val sy = sin(a) * radius
                        vertex(sx, sy)
                        a += angle
                    }
                    endShape(CLOSE)
                },
                50,
                "box"
        )
    }

    override fun onBecameActive() = Unit

    override fun setup() = Unit

    private var speed: Float = 0f

    override fun draw() = with(sketch) {
        val strokeNear = kontrol.knob1.midiRange(1f, 10f)
        val targetSpeed = kontrol.knob2.midiRange(1 / 10f, 4f)
        val step = kontrol.knob3.midiRange(100f, 200f).toInt()
        speed = lerp(speed, targetSpeed, 0.1f)

        background(bgColor)
        noFill()
        stroke(fgColor)
        translate(centerX(), centerY())

        for (z in 200 downTo -2600 step step) {
            val stroke = z.remap(0f, -2000f, strokeNear, strokeNear / 4f)
            val realZ = z + saw(speed).mapp(0f, step.toFloat())

            strokeWeight(stroke)
            pushMatrix()
            translate(0f, 0f, realZ)
            drawHexagon()
            popMatrix()
        }
    }

    private fun drawHexagon() = with(sketch) {
        hexagon.forEachIndexed { index, shape ->
            shape.disableStyle()
            shape(shape)
        }
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