package tools.galaxy.controls

import themidibus.MidiBus
import themidibus.SimpleMidiListener

class PushButton internal constructor(
        private val midiBus: MidiBus,
        private val ch: Int,
        private val cc: Int,
        private val onPress: () -> Unit
) {
    init {
        midiBus.addMidiListener(object : SimpleMidiListener {
            override fun noteOn(p0: Int, p1: Int, p2: Int) = Unit

            override fun noteOff(p0: Int, p1: Int, p2: Int) = Unit

            override fun controllerChange(channel: Int, control: Int, v: Int) {
                if (channel == ch && control == cc && v == 127) {
                    onPress.invoke()
                }
            }
        })
    }
}