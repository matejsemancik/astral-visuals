package dev.matsem.astral.core.tools.osc.delegates

import dev.matsem.astral.core.tools.osc.OscHandler
import dev.matsem.astral.core.tools.osc.OscManager
import oscP5.OscMessage
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Delegated [String] property that connects to OSC indicator represented a [String] value.
 * Suited for TouchOSC's Label indicator.
 */
class OscLabelIndicatorDelegate(
    private val oscManager: OscManager,
    private val address: String,
    defaultValue: String
) : ReadWriteProperty<OscHandler, String> {

    private var latestValue = defaultValue

    init {
        oscManager.sendMessage(OscMessage(address).add(defaultValue))
        oscManager.addMessageListener { message ->
            if (message.checkAddrPattern(address) && message.checkTypetag("s")) {
                latestValue = message[0].stringValue()
            }
        }
    }

    override fun getValue(thisRef: OscHandler, property: KProperty<*>): String = latestValue

    override fun setValue(thisRef: OscHandler, property: KProperty<*>, value: String) {
        if (latestValue == value) {
            return
        }

        latestValue = value
        oscManager.sendMessage(OscMessage(address).add(value))
    }
}