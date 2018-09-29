package tools.galaxy.controls

import midiRange
import themidibus.MidiBus
import themidibus.SimpleMidiListener

class Joystick internal constructor(
        private val midiBus: MidiBus,
        private val ch: Int,
        private val ccX: Int,
        private val ccY: Int,
        private val ccTouch: Int
) {

    var x: Float = 0f
    var y: Float = 0f
    var flipped = false

    init {
        center()

        midiBus.addMidiListener(object : SimpleMidiListener {
            override fun noteOn(p0: Int, p1: Int, p2: Int) = Unit

            override fun noteOff(p0: Int, p1: Int, p2: Int) = Unit

            override fun controllerChange(channel: Int, cc: Int, v: Int) {
                if (channel == ch) {
                    when (cc) {
                        ccX -> {
                            if (!flipped) {
                                x = v.midiRange(-1f, 1f)
                            } else {
                                y = v.midiRange(-1f, 1f)
                            }
                        }
                        ccY -> {
                            if (!flipped) {
                                y = v.midiRange(-1f, 1f)
                            } else {
                                x = v.midiRange(-1f, 1f)
                            }
                        }
                        ccTouch -> {
                            if (v == 0) {
                                center()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun center() {
        midiBus.sendControllerChange(ch, ccX, 127 / 2)
        midiBus.sendControllerChange(ch, ccY, 127 / 2)

        x = 0f
        y = 0f
    }
}