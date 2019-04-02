package dev.matsem.astral.tools.galaxy.controls

import themidibus.MidiBus
import dev.matsem.astral.tools.galaxy.SimpleMidiListenerAdapter

class ToggleButton internal constructor(
        private val midiBus: MidiBus,
        private val ch: Int,
        private val cc: Int,
        private val defaultValue: Boolean = false
) : MidiControl() {

    var isPressed = false

    init {
        isPressed = defaultValue
        midiBus.sendControllerChange(ch, cc, if (isPressed) 127 else 0)

        midiBus.addMidiListener(object : SimpleMidiListenerAdapter() {
            override fun controllerChange(channel: Int, control: Int, v: Int) {
                if (channel == ch && control == cc) {
                    isPressed = v == 127
                }
            }
        })
    }

    override fun sendClientUpdate() {
        midiBus.sendControllerChange(ch, cc, if (isPressed) 127 else 0)
    }
}