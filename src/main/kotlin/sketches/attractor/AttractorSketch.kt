package sketches.attractor

import centerX
import centerY
import midiRange
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector
import tools.kontrol.KontrolF1

class AttractorSketch : PApplet() {

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

    lateinit var kontrol: KontrolF1

    override fun settings() {
        size(1280, 720, PConstants.P3D)
    }

    override fun setup() {
        colorMode(PConstants.HSB, 360f, 100f, 100f, 100f)
        color = color(190f, 100f, 100f)
        color2 = color(160f, 100f, 100f)

        kontrol = KontrolF1().apply {
            connect()
        }

        attractor()
    }

    override fun draw() {
        a = kontrol.knob1.midiRange(-2f, 2f)
        b = kontrol.knob2.midiRange(-20f, 20f)
        c = kontrol.knob3.midiRange(-10f, 10f)
//        b = sin(millis() / 10000f) * 20f
//        c = sin(millis() / 12000f) * 20f
        d = kontrol.knob4.midiRange(-2f, 2f)
        e = kontrol.slider1.midiRange(0f, 10f)
        f = kontrol.slider2.midiRange(0f, 1f)

        attractor()

        background(color(0f, 0f, 0f, 0.01f))
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