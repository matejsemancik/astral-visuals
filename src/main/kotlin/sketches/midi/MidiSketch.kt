package sketches.midi

import centerX
import centerY
import processing.core.PApplet
import tools.KontrolF1

class MidiSketch : PApplet(), KontrolF1.F1ControlChangeReceiver {

    val f1 = KontrolF1()

    override fun settings() {
        size(400, 400)
        f1.ccReceiver = this
    }

    override fun setup() {

    }

    override fun draw() {
        translate(centerX(), centerY())
        background(0f)
    }

    override fun onF1ControlChange(channel: Int, number: Int, value: Int) {
        println("$channel $number $value")
    }
}