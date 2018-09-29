package tools.galaxy.controls

import themidibus.MidiBus
import themidibus.SimpleMidiListener

class ToggleButton internal constructor(
        private val midiBus: MidiBus,
        private val ch: Int,
        private val cc: Int,
        private val defaultValue: Boolean = false
) {

    var isPressed = false

    init {
        isPressed = defaultValue
        midiBus.sendControllerChange(ch, cc, if (isPressed) 127 else 0)

        midiBus.addMidiListener(object : SimpleMidiListener {
            override fun noteOn(p0: Int, p1: Int, p2: Int) = Unit

            override fun noteOff(p0: Int, p1: Int, p2: Int) = Unit

            override fun controllerChange(channel: Int, control: Int, v: Int) {
                if (channel == ch && control == cc) {
                    isPressed = v == 127
                }
            }
        })
    }
}