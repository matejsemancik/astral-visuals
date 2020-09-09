package dev.matsem.astral.core.tools.osc.controls

import dev.matsem.astral.core.tools.osc.OscManager
import oscP5.OscMessage

/**
 * OSC control represented by a single Float value with onChanged callback
 */
open class ListenableOscFader(
    private val oscManager: OscManager,
    private val address: String,
    defaultValue: Float,
    onChanged: (Float) -> Unit
) {

    var value: Float = defaultValue
        private set

    init {
        oscManager.sendMessage(OscMessage(address).add(defaultValue))
        oscManager.addMessageListener { message ->
            if (message.checkAddrPattern(address) && message.checkTypetag("f")) {
                value = message[0].floatValue()
                onChanged(message[0].floatValue())
            }
        }
    }

    fun setValue(value: Float) {
        if (value == this.value) {
            return
        }
        this.value = value
        oscManager.sendMessage(OscMessage(address).add(value))
    }
}