package tools

import processing.core.PApplet
import themidibus.MidiBus

class KontrolF1 {

    var ccReceiver: F1ControlChangeReceiver? = null

    companion object {
        const val KNOB1 = 2
        const val KNOB2 = 3
        const val KNOB3 = 4
        const val KNOB4 = 5

        const val SLIDER1 = 6
        const val SLIDER2 = 7
        const val SLIDER3 = 8
        const val SLIDER4 = 9

        const val ENCODER = 41
        const val ENCODER_DISPLAY = 41

        val PAD_MATRIX = mapOf(
                10 to (0 to 0),
                11 to (1 to 0),
                12 to (2 to 0),
                13 to (3 to 0),

                14 to (0 to 1),
                15 to (1 to 1),
                16 to (2 to 1),
                17 to (3 to 1),

                18 to (0 to 2),
                19 to (1 to 2),
                20 to (2 to 2),
                21 to (3 to 2),

                22 to (0 to 3),
                23 to (1 to 3),
                24 to (2 to 3),
                25 to (3 to 3)

        )

        fun padCoord(cc: Int): Pair<Int, Int> {
            return PAD_MATRIX.getOrDefault(cc, 0 to 0)
        }

        fun padCC(coords: Pair<Int, Int>): Int {
            PAD_MATRIX.forEach {
                if (it.value.equals(coords)) {
                    return it.key
                }
            }

            return 0
        }
    }

    var knob1 = 64
    var knob2 = 64
    var knob3 = 64
    var knob4 = 64

    var slider1 = 0
    var slider2 = 0
    var slider3 = 0
    var slider4 = 0

    var encoder = 0

    val padsRaw = arrayOf(
            intArrayOf(0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0)
    )

    val padsToggle = arrayOf(
            arrayOf(false, false, false, false),
            arrayOf(false, false, false, false),
            arrayOf(false, false, false, false),
            arrayOf(false, false, false, false)
    )

    var buttonColorOn = 127
    val midibus = MidiBus(this, "Traktor Kontrol F1 - 1 Input", "Traktor Kontrol F1 - 1 Output")

    init {
        for (x in 0..3) {
            for (y in 0..3) {
                sendHSV(padCC(x to y), 0, 0, 0)
            }
        }
    }

    // MidiBus override
    fun controllerChange(channel: Int, cc: Int, value: Int) {
        ccReceiver?.onF1ControlChange(channel, cc, value)

        when (cc) {
            KNOB1 -> knob1 = value
            KNOB2 -> knob2 = value
            KNOB3 -> knob3 = value
            KNOB4 -> knob4 = value

            SLIDER1 -> slider1 = value
            SLIDER2 -> slider2 = value
            SLIDER3 -> slider3 = value
            SLIDER4 -> slider4 = value

            ENCODER -> {
                encoder = value
                buttonColorOn = value

                for (x in 0..3) {
                    for (y in 0..3) {
                        if (padsToggle[x][y]) {
                            sendHSV(padCC(x to y), buttonColorOn, 127, 127)
                        }
                    }
                }
            }
        }

        if (PAD_MATRIX.containsKey(cc)) {
            val x = padCoord(cc).first
            val y = padCoord(cc).second

            padsRaw[x][y] = value
            if (value == 127) {
                padsToggle[x][y] = !padsToggle[x][y]
            }

            if (padsToggle[x][y]) {
                sendHSV(cc, buttonColorOn, 127, 127)
            } else {
                sendHSV(cc, buttonColorOn, 127, 0)
            }
        }
    }

    fun sendHSV(cc: Int, hue: Int, sat: Int, brightness: Int) {
        midibus.sendControllerChange(0, cc, hue) // Hue
        midibus.sendControllerChange(1, cc, sat) // Saturation
        midibus.sendControllerChange(2, cc, brightness) // Brightness
    }

    fun setCCReceiver(ccReceiver: F1ControlChangeReceiver) {
        this.ccReceiver = ccReceiver
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