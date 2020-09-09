package dev.matsem.astral.core.tools.osc.controls

import dev.matsem.astral.core.tools.osc.OscManager
import oscP5.OscMessage

/**
 * OSC toggle button control represented with a single Float value, where 0f is turned off and 1f is turned on.
 */
class OscToggleButton(
    private val oscManager: OscManager,
    private val address: String,
    defaultValue: Boolean
) {

    var value: Boolean = defaultValue
        private set

    init {
        oscManager.sendMessage(OscMessage(address).add(if (defaultValue) 1f else 0f))
        oscManager.addMessageListener { message ->
            if (message.checkAddrPattern(address) && message.checkTypetag("f")) {
                value = message[0].floatValue() == 1f
            }
        }
    }

    fun setValue(value: Boolean) {
        if (value == this.value) {
            return
        }
        this.value = value
        oscManager.sendMessage(OscMessage(address).add(value))
    }
}