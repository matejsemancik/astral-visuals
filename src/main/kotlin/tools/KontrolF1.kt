package tools

import processing.core.PApplet
import themidibus.MidiBus

class KontrolF1 {

    var ccReceiver: F1ControlChangeReceiver? = null

    companion object {
        val MATRIX = arrayOf(
                intArrayOf(10, 11, 12, 13),
                intArrayOf(14, 15, 16, 17),
                intArrayOf(18, 19, 20, 21),
                intArrayOf(22, 23, 24, 25)
        )

        const val KNOB1 = 2
        const val KNOB2 = 3
        const val KNOB3 = 4
        const val KNOB4 = 5

        const val SLIDER1 = 6
        const val SLIDER2 = 7
        const val SLIDER3 = 8
        const val SLIDER4 = 9
    }

    var knob1 = 64
    var knob2 = 64
    var knob3 = 64
    var knob4 = 64

    var slider1 = 0
    var slider2 = 0
    var slider3 = 0
    var slider4 = 0

    val midibus = MidiBus(this, "Traktor Kontrol F1 - 1 Input", "Traktor Kontrol F1 - 1 Output")

    fun sendHSV(cc: Int, hue: Int, sat: Int, brightness: Int) {
        midibus.sendControllerChange(0, cc, hue) // Hue
        midibus.sendControllerChange(1, cc, sat) // Saturation
        midibus.sendControllerChange(2, cc, brightness) // Brightness
    }

    fun setCCReceiver(ccReceiver: F1ControlChangeReceiver) {
        this.ccReceiver = ccReceiver
    }

    // MidiBus override
    fun controllerChange(channel: Int, number: Int, value: Int) {
        ccReceiver?.onF1ControlChange(channel, number, value)

        when (number) {
            KNOB1 -> knob1 = value
            KNOB2 -> knob2 = value
            KNOB3 -> knob3 = value
            KNOB4 -> knob4 = value

            SLIDER1 -> slider1 = value
            SLIDER2 -> slider1 = value
            SLIDER3 -> slider1 = value
            SLIDER4 -> slider1 = value
        }
    }

    // region Interfaces

    interface F1ControlChangeReceiver {
        fun onF1ControlChange(channel: Int, number: Int, value: Int)
    }

    // endregion
}

fun Int.midiRange(start: Float, end: Float): Float {
    return PApplet.map(this.toFloat(), 0f, 127f, start, end)
}

fun Int.midiRange(top: Float): Float {
    return this.midiRange(0f, top)
}