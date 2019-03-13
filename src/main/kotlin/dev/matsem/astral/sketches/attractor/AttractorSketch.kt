package dev.matsem.astral.sketches.attractor

import dev.matsem.astral.centerX
import dev.matsem.astral.centerY
import dev.matsem.astral.midiRange
import dev.matsem.astral.sketches.BaseSketch
import dev.matsem.astral.sketches.SketchLoader
import dev.matsem.astral.tools.kontrol.KontrolF1
import org.koin.core.inject
import processing.core.PVector

// TODO use Galaxy controls
class AttractorSketch : BaseSketch() {

    override val sketch: SketchLoader by inject()
    val kontrol: KontrolF1 by inject()

    var a = -2.24f
    var b = 0.43f
    var c = -0.65f
    var d = -2.43f
    var e = 0f
    var f = 0f

    var color = 0
    var color2 = 0
    var iterations = 15000

    val points = Array(iterations) { PVector() }

    override fun setup() {
        attractor()
    }

    override fun draw() {
        a = kontrol.knob1.midiRange(-2f, 2f)
        b = kontrol.knob2.midiRange(-20f, 20f)
        c = kontrol.knob3.midiRange(-10f, 10f)
        d = kontrol.knob4.midiRange(-2f, 2f)
        e = kontrol.slider1.midiRange(0f, 10f)
        f = kontrol.slider2.midiRange(0f, 1f)

        attractor()

        background(0)
        translate(centerX(), centerY())

        pushMatrix()
        stroke(color)
        fill(color)
        rotateY(millis() / 1000f)

        points.forEach {
            pushMatrix()
            translate(it.x * 200f, it.y * 200f, it.z * 200f)
            point(0f, 0f, 0f)
            popMatrix()
        }
        popMatrix()

        pushMatrix()
        stroke(color2)
        fill(color2)
        rotateY(-millis() / 1000f)

        points.forEach {
            pushMatrix()
            translate(it.x * 200f, it.y * 200f, it.z * 200f)
            point(0f, 0f, 0f)
            popMatrix()
        }
        popMatrix()
    }

    private fun attractor() {
        var px = 0f
        var py = 0f
        var pz = 0f

        for (i in 0 until iterations) {
            val bSin = b + sin(millis() / 10f) * 0.01f
            val cSin = c + sin(millis() / 12f) * 0.03f
            val x = sin(a * py) - cos(bSin * px)
            val y = sin(cSin * px) - cos(d * py)
            val z = sin(e * px) - cos(f * pz)

            px = x
            py = y
            pz = z

            points[i] = PVector(x, y, z)
        }
    }
}