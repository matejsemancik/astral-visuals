package dev.matsem.astral.core.tools.osc.delegates

import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import oscP5.OscMessage
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Delegated Boolean property for OSC toggle button represented with a single Float value, where 0f is turned off
 * and 1f is turned on.
 */
class OscToggleButtonDelegate(
    private val oscManager: OscManager,
    private val address: String,
    defaultValue: Boolean
) : ReadWriteProperty<OscHandler, Boolean> {

    private var latestValue = defaultValue

    init {
        oscManager.sendMessage(OscMessage(address).add(if (defaultValue) 1f else 0f))
        oscManager.addMessageListener { message ->
            if (message.checkAddrPattern(address) && message.checkTypetag("f")) {
                latestValue = message[0].floatValue() == 1f
            }
        }
    }

    override fun getValue(thisRef: OscHandler, property: KProperty<*>): Boolean = latestValue

    override fun setValue(thisRef: OscHandler, property: KProperty<*>, value: Boolean) {
        latestValue = value
        oscManager.sendMessage(OscMessage(address).add(if (value) 1f else 0f))
    }
}