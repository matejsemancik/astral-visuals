package tools.galaxy.controls

import midiRange
import midiValue
import themidibus.MidiBus
import tools.galaxy.SimpleMidiListenerAdapter

class Joystick internal constructor(
        private val midiBus: MidiBus,
        private val ch: Int,
        private val ccX: Int,
        private val ccY: Int,
        private val ccTouchXY: Int,
        private val ccZ: Int,
        private val ccTouchZ: Int,
        private val ccFeedbackToggle: Int
) : MidiControl() {

    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f
    var flipped = false
    var feedbackEnabled = true

    init {
        sendInitialState()

        midiBus.addMidiListener(object : SimpleMidiListenerAdapter() {
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
                        ccZ -> {
                            z = v.midiRange(-1f, 1f)
                        }
                        ccFeedbackToggle -> {
                            feedbackEnabled = v == 127
                            if (feedbackEnabled) {
                                centerAll()
                            }
                        }
                        ccTouchXY -> {
                            if (v == 0 && feedbackEnabled) {
                                centerXY()
                            }
                        }
                        ccTouchZ -> {
                            if (v == 0 && feedbackEnabled) {
                                centerZ()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun sendInitialState() {
        midiBus.sendControllerChange(ch, ccFeedbackToggle, feedbackEnabled.midiValue())
        centerAll()
    }

    private fun centerAll() {
        centerXY()
        centerZ()
    }

    private fun centerXY() {
        midiBus.sendControllerChange(ch, ccX, 127 / 2)
        midiBus.sendControllerChange(ch, ccY, 127 / 2)
        x = 0f
        y = 0f
    }

    private fun centerZ() {
        midiBus.sendControllerChange(ch, ccZ, 127 / 2)
        z = 0f
    }
}