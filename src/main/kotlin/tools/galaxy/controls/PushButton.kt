package tools.galaxy.controls

import themidibus.MidiBus
import tools.galaxy.SimpleMidiListenerAdapter

class PushButton internal constructor(
        private val midiBus: MidiBus,
        private val ch: Int,
        private val cc: Int,
        private val onPress: () -> Unit
) {
    init {
        midiBus.addMidiListener(object : SimpleMidiListenerAdapter() {
            override fun controllerChange(channel: Int, control: Int, v: Int) {
                if (channel == ch && control == cc && v == 127) {
                    onPress.invoke()
                }
            }
        })
    }
}