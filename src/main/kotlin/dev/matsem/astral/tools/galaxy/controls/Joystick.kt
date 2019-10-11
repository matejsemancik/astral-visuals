package dev.matsem.astral.tools.galaxy.controls

import dev.matsem.astral.tools.extensions.midiRange
import dev.matsem.astral.tools.extensions.midiValue
import dev.matsem.astral.tools.extensions.toMidi
import themidibus.MidiBus

class Joystick internal constructor(
        private val midiBus: MidiBus,
        private val ch: Int,
        private val ccX: Int,
        private val ccY: Int,
        private val ccTouchXY: Int,
        private val ccZ: Int,
        private val ccTouchZ: Int,
        private val ccFeedbackToggle: Int
) : GalaxyControl {

    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f
    var flipped = false
    var feedbackEnabled = true

    init {
        sendInitialState()
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

    fun flipped() = apply {
        flipped = true
    }

    override fun controllerChange(channel: Int, control: Int, v: Int) {
        if (channel == ch) {
            when (control) {
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

    override fun update() = Unit

    override fun updatePhone() {
        midiBus.sendControllerChange(ch, if (!flipped) ccX else ccY, x.toMidi(-1f, 1f))
        midiBus.sendControllerChange(ch, if (!flipped) ccY else ccX, y.toMidi(-1f, 1f))
        midiBus.sendControllerChange(ch, ccZ, z.toMidi(-1f, 1f))
    }
}